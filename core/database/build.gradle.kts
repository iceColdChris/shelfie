plugins {
    id("shelfie.android.library")
    id("shelfie.android.hilt")
}

android {
    namespace = "com.shelfie.core.database"
}

dependencies {
    // Room
    implementation(libs.room.runtime)
    implementation(libs.sqlite.framework)
    ksp(libs.room.compiler)

    // Moshi, for the List<String> type converter
    implementation(libs.moshi.kotlin)

    // Coroutines
    implementation(libs.coroutines.android)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.turbine)
}
