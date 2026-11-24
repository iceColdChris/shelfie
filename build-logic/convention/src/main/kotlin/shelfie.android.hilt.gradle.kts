// Applied imperatively: the Hilt plugin needs an Android plugin on the project first,
// which a `plugins {}` block here cannot guarantee.
apply(plugin = "com.google.devtools.ksp")
apply(plugin = "com.google.dagger.hilt.android")

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(libs.findLibrary("hilt-android").get())
    "ksp"(libs.findLibrary("hilt-compiler").get())
}
