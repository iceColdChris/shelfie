plugins {
    `kotlin-dsl`
}

// Every Gradle plugin the conventions apply. Modules apply them without a version
// because they are already on the build classpath through this build.
dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
}
