plugins {
    id("shelfie.android.feature")
}

android {
    namespace = "com.shelfie.feature.books"
}

dependencies {
    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Shimmer
    implementation(libs.shimmer.compose)
}
