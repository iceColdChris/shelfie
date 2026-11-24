# Commands

- Unit tests, all modules: `./gradlew testDebugUnitTest` (JVM modules `core:model`/`core:testing` have no tests)
- One module: `./gradlew :core:data:testDebugUnitTest` (also `:core:domain`, `:feature:books`, ...)
- App build (also validates the Hilt graph): `./gradlew :app:assembleDebug`
- DAO instrumented tests (device needed): `./gradlew :core:database:connectedDebugAndroidTest`; compile-only check: `./gradlew :core:database:compileDebugAndroidTestKotlin`
- Lint: `./gradlew lintDebug`
- With factory-mcp present, prefer `android_test` / `android_build` / `android_audit` (module = directory, `.` for all); git goes through `core_git_*`.
- Test counts for module `.` come back 0 from `android_test`; read `*/build/test-results/testDebugUnitTest/*.xml`.
