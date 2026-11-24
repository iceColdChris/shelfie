# Task completion

1. `./gradlew testDebugUnitTest` green.
2. `./gradlew :app:assembleDebug` green (catches missing Hilt bindings and module-boundary breaks).
3. Touched Room code: `./gradlew :core:database:compileDebugAndroidTestKotlin`; run connected tests if a device is attached.
4. New/moved module or rule change: update `AGENT_GUIDE.md`, `README.md`, and `.github/workflows/*` task paths.
5. `android_audit` if available. Known pre-existing findings, not regressions: BLD-003 (release R8 off), CORO-003 (`catch (e: Exception)` in BooksViewModel/BookDetailViewModel).
