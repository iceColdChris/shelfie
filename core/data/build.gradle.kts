plugins {
    id("shelfie.android.library")
    id("shelfie.android.hilt")
}

android {
    namespace = "com.shelfie.core.data"
}

dependencies {
    // Repository interfaces return model types.
    api(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))

    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
