plugins {
    id("shelfie.android.library.compose")
}

android {
    namespace = "com.shelfie.core.designsystem"
}

dependencies {
    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
