plugins {
    id("shelfie.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The type-safe `libs` accessor does not exist inside a convention plugin.
val libs = the<VersionCatalogsExtension>().named("libs")

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    "implementation"(platform(libs.findLibrary("compose-bom").get()))
    "implementation"(libs.findLibrary("compose-ui").get())
    "implementation"(libs.findLibrary("compose-ui-tooling-preview").get())
    "implementation"(libs.findLibrary("compose-material3").get())
    "debugImplementation"(libs.findLibrary("compose-ui-tooling").get())
}
