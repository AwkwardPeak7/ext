plugins {
    id("com.android.library")
}

android {
    compileSdk = AndroidConfig.compileSdk

    defaultConfig {
        minSdk = AndroidConfig.minSdk
    }

    namespace = "keiyoushi.core"

    sourceSets {
        named("main") {
            manifest.srcFile("AndroidManifest.xml")
            res.setSrcDirs(listOf("res"))
        }
    }

    buildFeatures {
        resValues = false
        shaders = false
    }
}
