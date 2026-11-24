plugins {
    id("shelfie.android.library")
    id("shelfie.android.hilt")
}

android {
    namespace = "com.shelfie.core.network"

    buildFeatures {
        // NetworkModule reads BuildConfig.DEBUG to enable HTTP logging.
        buildConfig = true
    }
}

dependencies {
    // Retrofit + OkHttp + Moshi
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)

    // Timber
    implementation(libs.timber)
}
