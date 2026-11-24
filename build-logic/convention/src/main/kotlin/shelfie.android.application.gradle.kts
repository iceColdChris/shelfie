// The app module's own android {} block stays in app/build.gradle.kts; this only puts
// the plugins on its classpath, which a plain id("com.android.application") cannot do.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
