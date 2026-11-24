plugins {
    id("shelfie.android.library")
    id("shelfie.android.hilt")
}

android {
    namespace = "com.shelfie.core.domain"
}

dependencies {
    // Use cases return model types.
    api(project(":core:model"))
    // Not `api`: features must not see repositories through this module.
    implementation(project(":core:data"))

    implementation(libs.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
