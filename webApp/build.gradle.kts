plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig { outputFileName = "dungeonsanddeigo.js" }
        }
        binaries.executable()
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            implementation(npm("sass-loader", "14.2.1"))
            implementation(npm("sass", "1.77.8"))
            implementation(npm("style-loader", "4.0.0"))
            implementation(npm("css-loader", "7.1.2"))
        }
    }
}

// Copy fonts from project root to webApp resources
val copyFonts by tasks.registering(Copy::class) {
    from(rootProject.file("fonts"))
    into(file("src/wasmJsMain/resources/fonts"))
    include("**/*.ttf", "**/*.otf", "**/*.woff", "**/*.woff2")
}

tasks.named("wasmJsProcessResources") {
    dependsOn(copyFonts)
}
