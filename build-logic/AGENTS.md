# Gradle build logic

- Module `build.gradle.kts` = one `shelfie.*` convention + `namespace` + module deps. SDK levels, JVM toolchain and Compose come from the conventions; levels live in `convention/src/main/kotlin/ShelfieBuild.kt`.
- New Gradle plugin: add a `*-gradlePlugin` library to `gradle/libs.versions.toml`, `implementation` it in `convention/build.gradle.kts`, apply it inside a convention. The catalog has no `[plugins]`; a module requesting a plugin id directly fails with "plugin not found" (why `kotlin.plugin.serialization` sits in `shelfie.android.application`).
- Inside a convention: catalog via `the<VersionCatalogsExtension>().named("libs")`; configurations as strings (`"implementation"(...)`).
- `shelfie.android.hilt` applies KSP + Hilt imperatively: list it after the Android convention.
- AGP 9 has built-in Kotlin: leave `org.jetbrains.kotlin.android` out. Configure Kotlin with top-level `kotlin {}`, variants with `androidComponents {}`.
- `compileSdk { version = release(N) { minorApiLevel = M } }`. Plain `compileSdk = 37` means 37.0; current AndroidX needs 37.1+ and fails `checkDebugAarMetadata`.
- Configuration cache is on (`gradle.properties`): new build logic must be configuration-cache compatible.
- Version checks: Room 3 is a new group, `androidx.room3:room3-*`; querying `androidx.room` reports 2.x as latest.
