plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    // Navigation 3 routes are @Serializable NavKeys, and the app module owns them.
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    compileSdk {
        version = release(ShelfieBuild.COMPILE_SDK) {
            minorApiLevel = ShelfieBuild.COMPILE_SDK_MINOR
        }
    }

    defaultConfig {
        minSdk = ShelfieBuild.MIN_SDK
        targetSdk = ShelfieBuild.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(ShelfieBuild.JVM_TOOLCHAIN)
}
