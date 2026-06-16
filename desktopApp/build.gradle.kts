plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.composenativetray)
}

compose.desktop {
    application {
        mainClass = "com.lemon.focuspet.MainKt"
        jvmArgs("-Dfile.encoding=GBK")

        nativeDistributions {
            packageName = "FocusPet"
            packageVersion = "1.0.0"
            modules("java.base")
        }
    }
}

// ── packagePortable：一行命令打便携版 ────────────────────────
// 命令：.\gradlew :desktopApp:packagePortable
// 输出：desktopApp/build/compose/dist/FocusPet/FocusPet.exe
//
// 它做了三件事（自动串联）：
//   ① packageUberJarForCurrentOS → 把所有依赖打进一个 fat JAR
//   ② createRuntimeImage        → 用 jlink 裁剪 JRE，只留实际用到的模块
//   ③ jpackage --type app-image → 把 JAR + JRE 封装成 FocusPet.exe
//
// 产物 ≈110 MB（其中 JRE 占 ~70 MB），双击即运行，不安装。
// ───────────────────────────────────────────────────────────
val jpackage = file(System.getProperty("java.home")).resolve("bin/jpackage.exe")

tasks.register<Exec>("packagePortable") {
    dependsOn("packageUberJarForCurrentOS", "createRuntimeImage")

    val jarDir = layout.buildDirectory.dir("compose/jars")
    val jarName = "FocusPet-windows-x64-1.0.0.jar"
    val runtimeDir = layout.buildDirectory.dir("compose/tmp/main/runtime")
    val outDir = layout.buildDirectory.dir("compose/dist")

    inputs.dir(jarDir)
    inputs.dir(runtimeDir)
    outputs.dir(outDir)

    commandLine(
        jpackage.absolutePath,
        "--type", "app-image",                 // 便携版文件夹（非安装包）
        "--input", jarDir.get().asFile.absolutePath,
        "--main-jar", jarName,                 // 上一步产出的 fat JAR
        "--main-class", "com.lemon.focuspet.MainKt",
        "--name", "FocusPet",
        "--dest", outDir.get().asFile.absolutePath,
        "--runtime-image", runtimeDir.get().asFile.absolutePath, // 裁剪后的 JRE
        "--java-options", "-Dfile.encoding=GBK",
        "--java-options", "--enable-native-access=ALL-UNNAMED",  // 去掉 JDK 24 警告
    )
}