plugins {
    id("shelfie.android.library.compose")
    id("shelfie.android.hilt")
}

val libs = the<VersionCatalogsExtension>().named("libs")

// A feature is a UI module: screens and view models. It reaches data only through
// :core:domain use cases, so it never depends on :core:data, :core:database or :core:network.
dependencies {
    "implementation"(project(":core:designsystem"))
    "implementation"(project(":core:domain"))
    "implementation"(project(":core:model"))

    "implementation"(libs.findLibrary("hilt-lifecycle-viewmodel-compose").get())
    "implementation"(libs.findLibrary("compose-material-icons").get())
    "implementation"(libs.findLibrary("compose-material-icons-extended").get())
    "implementation"(libs.findLibrary("lifecycle-runtime-compose").get())
    "implementation"(libs.findLibrary("lifecycle-viewmodel-compose").get())
    "implementation"(libs.findLibrary("coroutines-android").get())
    "implementation"(libs.findLibrary("kotlinx-collections-immutable").get())

    "testImplementation"(project(":core:testing"))
    "testImplementation"(libs.findLibrary("mockk").get())
    "testImplementation"(libs.findLibrary("turbine").get())
}
