plugins {
    id("shelfie.android.feature")
}

android {
    namespace = "com.shelfie.feature.stats"
}

dependencies {
    // Lottie
    implementation(libs.lottie.compose)
}
