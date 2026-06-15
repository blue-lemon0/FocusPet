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
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "FocusPet"
            packageVersion = "1.0.0"
        }
    }
}