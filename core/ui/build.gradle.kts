plugins {
    id("shelfie.android.library.compose")
}

android {
    namespace = "com.shelfie.core.ui"
}

dependencies {
    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
