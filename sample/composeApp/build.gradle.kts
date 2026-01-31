import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()
    js  {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(project(":shared"))
            
            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "LovelyBible"
            packageVersion = "1.0.1"
            description = "Bible Presentation App"
            copyright = "Copyright 2026 LovelyBible"
            vendor = "LovelyBibleTeam"
            
            windows {
                menuGroup = "LovelyBible"
                shortcut = true
                dirChooser = false
                perUserInstall = true
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
            }
        }
    }
}
