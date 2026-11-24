plugins {
    id("shelfie.jvm.library")
}

dependencies {
    api(libs.kotlinx.collections.immutable)

    // Annotations only (@Immutable), so the models stay pure Kotlin with no Compose runtime.
    api(platform(libs.compose.bom))
    api(libs.compose.runtime.annotation)
}
