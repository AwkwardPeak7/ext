import gradle.kotlin.dsl.accessors._1c35da307f1540a2fdd9273b146bf0a7.kotlin

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlinx-serialization")
    id("org.jmailen.kotlinter")
}

android {
    compileSdk = AndroidConfig.compileSdk

    defaultConfig {
        minSdk = AndroidConfig.minSdk
    }

    namespace = "eu.kanade.tachiyomi.multisrc.${project.name}"

    sourceSets {
        named("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.setSrcDirs(listOf("src"))
            res.setSrcDirs(listOf("res"))
            assets.setSrcDirs(listOf("assets"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

kotlinter {
    experimentalRules = true
    disabledRules = arrayOf(
        "experimental:argument-list-wrapping", // Doesn't play well with Android Studio
        "experimental:comment-wrapping",
    )
}

dependencies {
    compileOnly(versionCatalogs.named("libs").findBundle("common").get())
}

tasks {
    preBuild {
        dependsOn(lintKotlin)
    }

    if (System.getenv("CI") != "true") {
        lintKotlin {
            dependsOn(formatKotlin)
        }
    }
}
