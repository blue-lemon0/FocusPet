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

            windows {
                iconFile.set(rootProject.projectDir.resolve("shared/src/jvmMain/resources/icons/icon.ico"))
            }
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
    val iconFile = rootProject.projectDir.resolve("shared/src/jvmMain/resources/icons/icon.ico")

    inputs.dir(jarDir)
    inputs.dir(runtimeDir)
    inputs.file(iconFile)
    outputs.dir(outDir)

    commandLine(
        jpackage.absolutePath,
        "--type", "app-image",
        "--input", jarDir.get().asFile.absolutePath,
        "--main-jar", jarName,
        "--main-class", "com.lemon.focuspet.MainKt",
        "--name", "FocusPet",
        "--dest", outDir.get().asFile.absolutePath,
        "--runtime-image", runtimeDir.get().asFile.absolutePath,
        "--java-options", "-Dfile.encoding=GBK",
        "--java-options", "--enable-native-access=ALL-UNNAMED",
    )
}

// ── patchIcon：修正 EXE 图标 ──────────────────────────
// jpackage --type app-image 的 --icon 参数在 Windows 上有 bug，
// 生成的 EXE 图标仍是默认 Java 图标。此处用 Windows API
// UpdateResourceW 重新写入 RT_GROUP_ICON + RT_ICON 资源。
// ───────────────────────────────────────────────────────
val pythonExec = "python"
val patchScript = rootProject.projectDir.resolve("patch_icon.py")
val icoFile = rootProject.projectDir.resolve("shared/src/jvmMain/resources/icons/icon.ico")

tasks.register("patchIcon") {
    dependsOn("packagePortable")
    val exe = layout.buildDirectory.file("compose/dist/FocusPet/FocusPet.exe")
    inputs.file(icoFile)
    outputs.file(exe)

    doLast {
        // jpackage creates the exe with ReadOnly attribute;
        // remove it first so UpdateResourceW can write.
        exe.get().asFile.setWritable(true)

        exec {
            commandLine(
                pythonExec,
                patchScript.absolutePath,
                exe.get().asFile.absolutePath,
                icoFile.absolutePath,
            )
        }
    }
}

// 默认打便携版一步到位
tasks.named("packagePortable") {
    finalizedBy("patchIcon")
}