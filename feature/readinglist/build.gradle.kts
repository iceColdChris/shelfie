plugins {
    id("shelfie.android.feature")
}

android {
    namespace = "com.shelfie.feature.readinglist"
}

dependencies {
    // Reorderable
    implementation(libs.reorderable)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
