plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlinx-serialization")
}

android {
    compileSdk = AndroidConfig.compileSdk

    defaultConfig {
        minSdk = AndroidConfig.minSdk
    }

    namespace = "keiyoushix.lib.${project.name}"

    sourceSets {
        named("main") {
            java.setSrcDirs(listOf("src"))
            assets.setSrcDirs(listOf("assets"))
        }
    }

    buildFeatures {
        androidResources = false
    }
}

dependencies {
    compileOnly(versionCatalogs.named("libs").findBundle("common").get())
}
