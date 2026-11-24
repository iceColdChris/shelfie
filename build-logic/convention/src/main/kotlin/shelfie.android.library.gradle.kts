plugins {
    id("com.android.library")
}

android {
    compileSdk {
        version = release(ShelfieBuild.COMPILE_SDK) {
            minorApiLevel = ShelfieBuild.COMPILE_SDK_MINOR
        }
    }

    defaultConfig {
        minSdk = ShelfieBuild.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    jvmToolchain(ShelfieBuild.JVM_TOOLCHAIN)
}
