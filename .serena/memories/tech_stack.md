# Tech stack

Kotlin, Compose (Material 3), Hilt (KSP), Room, Retrofit + Moshi (reflection adapter), Coil, Navigation Compose (string routes), kotlinx.collections.immutable, `kotlin.time` Instant/Clock. Tests: JUnit 4, MockK, Turbine, coroutines-test. Versions: `gradle/libs.versions.toml`.

## Build gotchas (AGP 9, Gradle 9)
- AGP 9 built-in Kotlin: never apply `org.jetbrains.kotlin.android`.
- No `[plugins]` in the catalog and no `alias(...) apply false` in the root build: Gradle plugins are `*-gradlePlugin` library entries consumed by `build-logic/convention/build.gradle.kts`. A versioned plugin request in a module fails (already on classpath).
- A module cannot apply `com.android.application`/`library` by raw id; it must go through a `shelfie.*` convention (that is what puts the plugin on its classpath).
- Inside conventions: catalog via `the<VersionCatalogsExtension>().named("libs")`, string configuration names.
- `shelfie.android.hilt` applies KSP + Hilt imperatively (Hilt needs an Android plugin first); list it after the Android convention.
- `core:model` is JVM; `@Immutable` comes from `androidx.compose.runtime:runtime-annotation`, not the Compose runtime.
- `core:database` needs Moshi for `Converters` (List<String> <-> JSON).
- Room: `exportSchema = false`, destructive migration fallback; bump `version` on entity change.
