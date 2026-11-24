# :core:database - Room 3

- Package `androidx.room3`. Type converters are `@ColumnTypeConverter(s)`; a Room 2 name like `TypeConverters` makes KSP report only `[MissingType]` on `ShelfieDatabase`, and a clean build does not help.
- DAO functions are `suspend` or return `Flow`.
- Every builder sets the driver: `Room.databaseBuilder<ShelfieDatabase>(context, name).setDriver(AndroidSQLiteDriver())`; tests use `Room.inMemoryDatabaseBuilder<ShelfieDatabase>(context).setDriver(AndroidSQLiteDriver())`.
- Schema change: bump `@Database(version)`. `exportSchema = false` with destructive fallback; this is a dev app and local data is disposable.
- Table and column names are set explicitly (`tableName = ...`), so renaming an entity class leaves the schema unchanged. Renaming a table, column or stored value needs a version bump.
- `Shelf.value` strings are persisted: renaming one orphans rows, and `Shelf.fromValue` throws on unknown values.
- `List<String>` columns go through Moshi in `StringListConverter`.
- No device: `./gradlew :core:database:compileDebugAndroidTestKotlin` still catches DAO/test breakage.
- Device tests: `./gradlew :core:database:connectedDebugAndroidTest`. With 2+ entries in `adb devices`, set `ANDROID_SERIAL` (two serials can be one emulator; parallel runs crash it). Gradle can exit 0 after an instrumentation crash: confirm in `build/outputs/androidTest-results/connected/debug/TEST-*.xml`.
