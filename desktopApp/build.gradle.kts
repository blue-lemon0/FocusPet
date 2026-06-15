plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.lemon.focuspet.MainKt"
        jvmArgs("-Dfile.encoding=GBK")

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "FocusPet"
            packageVersion = "1.0.0"
        }
    }
}