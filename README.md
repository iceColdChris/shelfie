# Shelfie

A book-tracking Android app built with **Jetpack Compose**, **Hilt**, **Room**, and **Retrofit**.
It uses a multi-module Clean Architecture approach with an offline-first data strategy.

---

## Quick start

1. Open the project in Android Studio (Ladybug or later -- AGP 9+ is required).
2. Let Gradle sync finish.
3. Run the `:app` module on an emulator or device (API 26+).

> The app talks to the [Open Library API](https://openlibrary.org/developers/api), so an
> internet connection is needed for the initial book search. After that, results are cached
> locally in Room and the app works offline.

---

## Project structure

The project is split into **Gradle modules** so that features cannot accidentally depend on
each other and build times stay fast as the project grows.

```
:app                  -- Entry point: Application class, single Activity, NavHost, Hilt graph
:feature:books        -- Book search, favorites & book detail (UI only)
:feature:readinglist  -- Personal reading shelf management (UI only)
:feature:stats        -- Aggregated reading statistics (UI only)
:core:domain          -- Use cases: the only way a feature reaches data
:core:data            -- Repository interfaces, implementations and mappers
:core:database        -- Room database, DAOs, entities
:core:network         -- Retrofit/OkHttp/Moshi setup, Open Library API, DTOs
:core:model           -- Pure Kotlin domain models shared by every layer
:core:ui              -- Shared design system (theme, colors, typography)
:core:testing         -- Shared test rules (MainDispatcherRule)
build-logic           -- Convention plugins that configure all of the above
```

Dependencies point one way:

```
app -> feature -> core:domain -> core:data -> core:database, core:network
                      \______________\__________ core:model
```

A feature never depends on another feature, on `:core:data`, or on a data source. That is
enforced by Gradle, not by convention: `:core:domain` declares `:core:data` as
`implementation`, so repositories are not even on a feature's compile classpath.

---

## Module blueprints

A feature module holds only its UI layer:

```
com.shelfie.feature.<name>.ui
├── <Name>Screen.kt       Stateless @Composable entry point
├── <Name>ViewModel.kt    Hilt ViewModel -- owns the state, calls use cases
├── <Name>UiState.kt      Immutable data class describing what the screen shows
└── <Name>UiEvent.kt      Sealed interface for every user action
```

The layers below it are shared, because more than one feature needs the same data (the
reading list is read by all three):

```
com.shelfie.core.domain            Single-purpose use cases
com.shelfie.core.data
├── repository/                    Repository interfaces (public) + implementations (`internal`)
├── mapper/                        DTO/Entity -> model conversions (`internal`)
└── di/                            Hilt module binding interface to implementation
com.shelfie.core.database          AppDatabase, dao/, entity/, converter/, di/
com.shelfie.core.network           OpenLibraryApi, dto/, di/
com.shelfie.core.model             Pure Kotlin data classes -- the "truth" the UI works with
```

### Why this matters

Android newcomers often put everything in one package. Separating layers gives you:

- **Testability** -- you can unit-test a UseCase without spinning up a real database.
- **Compile isolation** -- changing UI code does not recompile the data layer.
- **Enforced boundaries** -- module dependencies and the `internal` keyword prevent a
  screen from reaching a DAO, a DTO or a repository implementation.

### Build configuration

Each module's `build.gradle.kts` applies one convention plugin from
`build-logic/convention` and adds only what is specific to it:

| Plugin | For |
|---|---|
| `shelfie.android.application` | `:app` |
| `shelfie.android.feature` | feature modules (Compose, Hilt, core modules, test libraries) |
| `shelfie.android.library` | Android core modules |
| `shelfie.android.library.compose` | Android modules with Compose UI |
| `shelfie.android.hilt` | any module with Hilt bindings or `@Inject` constructors |
| `shelfie.jvm.library` | pure Kotlin modules (`:core:model`, `:core:testing`) |

SDK levels and Java target are set once, in `shelfie.android.library`. Gradle plugin
versions live in `gradle/libs.versions.toml` as libraries that `build-logic` depends on.

---

## Key concepts

### The `usecase/` folder

A **UseCase** is a small class that does *one* business operation. ViewModels call UseCases
instead of calling repositories directly. This keeps ViewModel logic thin and makes each
operation independently testable.

```kotlin
// Example: GetShelfBooksUseCase.kt
class GetShelfBooksUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    operator fun invoke(shelf: Shelf): Flow<List<ReadingListBook>> =
        repository.observeByShelf(shelf)
}
```

Common conventions in this project:

- **`operator fun invoke(...)`** lets you call the UseCase like a function:
  `getShelfBooksUseCase(shelf)` instead of `getShelfBooksUseCase.execute(shelf)`.
- Some UseCases split observation from refresh (e.g. `SearchBooksUseCase` exposes both
  `observe()` and `refresh()`) to support the offline-first pattern.
- Every UseCase receives its dependencies through `@Inject constructor` so Hilt can
  provide them automatically.

### The `internal` keyword

Kotlin's `internal` means "visible only inside this Gradle module." In this project:

- Repository **implementations**, mappers, `AppDatabase`, and Hilt modules are `internal`.
- Repository **interfaces**, UseCases, and domain models are **public** because other modules
  need to see them. DAOs, entities, the Retrofit interface and DTOs are public too, but only
  `:core:data` depends on the modules that hold them.

If you forget `internal` on an implementation class, other modules could bypass the
interface and depend on concrete details -- defeating the whole point of the architecture.

### Offline-first data flow

The data always flows: **Network -> Room (local DB) -> UI**.

1. The UI observes a `Flow` from Room (the single source of truth).
2. A background coroutine fetches fresh data from the API and writes it into Room.
3. Room automatically pushes the update to the UI through the existing Flow.

This means the app shows cached data immediately and updates seamlessly when the
network responds. Look at `BookRepositoryImpl` for a concrete example.

### Unidirectional Data Flow (UDF)

State flows in one direction through the UI layer:

```
User action  ->  UiEvent  ->  ViewModel  ->  UiState  ->  Composable
```

- **`UiEvent`** -- a sealed interface with one subclass per user action (tap, type, etc.).
- **`UiState`** -- an immutable data class holding everything the screen needs to render.
- The ViewModel exposes a single `StateFlow<UiState>`. The composable never mutates
  state directly; it sends events to the ViewModel, which produces a new state.

### Dependency injection with Hilt

Hilt generates the wiring so you never manually create objects. Key things to know:

- **`@HiltAndroidApp`** on `SampleApp` (the `Application` class) -- bootstraps Hilt.
- **`@AndroidEntryPoint`** on `MainActivity` -- lets Hilt inject into the Activity.
- **`@HiltViewModel`** on ViewModels -- lets Hilt provide constructor dependencies.
- **`@InstallIn(SingletonComponent::class)`** for everything in the data layer: Retrofit and
  Room are `@Singleton`; repository bindings are unscoped because repositories hold no state.
- Feature modules have no Hilt modules. Their ViewModels get use cases by constructor injection.
- **`@Binds`** maps an interface to its implementation (e.g. `BookRepository` to
  `BookRepositoryImpl`).
- **`@Provides`** creates instances Hilt cannot construct on its own (Retrofit interfaces, etc.).

### Mappers

DTOs and Room entities should **never** leak into composables. The `mapper/` package
contains extension functions that convert between layers:

```
Network DTO  -->  Room Entity  -->  Domain Model  -->  UI
```

See `BookMapper.kt` for the pattern: each conversion is a small, pure extension function.

### Compose conventions

- Every screen composable has a `Content(...)` variant that takes the `UiState` and an
  event callback `(UiEvent) -> Unit`. This makes previews and tests trivial because you
  can render any state without a ViewModel.
- All composables accept a `Modifier` as their first optional parameter.
- Lists use `ImmutableList` from `kotlinx.collections.immutable` so Compose can skip
  recomposition when the list contents have not changed.

---

## Build toolchain notes (AGP 9)

This project uses Android Gradle Plugin **9.1.0+**, which has several differences from
older tutorials you might find online:

- **No `kotlin-android` plugin** -- AGP 9 bundles Kotlin. Applying `org.jetbrains.kotlin.android`
  will cause a conflict. The Compose compiler plugin (`kotlin-compose`) is still needed.
- **No `kotlinOptions` block** -- use a top-level `kotlin { compilerOptions { } }` block instead.
- **`BaseExtension` is removed** -- use the `androidComponents { }` DSL for variant
  configuration.

---

## Testing

### Test strategy

The project has two layers of tests that each catch different classes of bugs:

| Layer | Tool | What it tests | Where |
|---|---|---|---|
| Unit (JVM) | JUnit 4 + MockK + Turbine | Business logic in isolation | `src/test/` in `core/data`, `core/domain` and each feature module |
| DAO (instrumented) | JUnit 4 + Room in-memory | Real SQL queries and Flow reactivity | `core/database/src/androidTest/` |

> **JUnit 5** has no native Android support for instrumented tests and requires a third-party Gradle plugin for JVM-only tests. Given the project is stable on JUnit 4 (`4.13.2`, the current latest), migrating adds complexity with no meaningful benefit.

### Running unit tests

```bash
# All modules at once
./gradlew test

# A single module
./gradlew :feature:books:testDebugUnitTest
```

This covers **116 tests** across 14 classes:
- `core:data` — `BookMapper`, `ReadingListMapper`, `BookRepositoryImpl`, `ReadingListRepositoryImpl`, `StatsRepositoryImpl` (streak + monthly chart algorithms)
- `core:domain` — five use case tests
- `feature:books` — `BooksViewModel`, `BookDetailViewModel`
- `feature:readinglist` — `ReadingListViewModel`
- `feature:stats` — `StatsViewModel`

All ViewModel tests use the `MainDispatcherRule` from `:core:testing` (a JUnit4 `TestWatcher`) instead of manual `@Before/@After` dispatcher setup.

### Running DAO instrumented tests

A connected device or emulator is required.

```bash
./gradlew :core:database:connectedDebugAndroidTest
```

This covers **43 tests** across `BookDaoTest` and `ReadingListDaoTest` using `Room.inMemoryDatabaseBuilder`. Each test gets a fresh, isolated database via `@Before`/`@After`.

### CI (GitHub Actions)

- **Unit tests** run on every push and every pull request targeting `main` (fast, JVM-only, no device needed).
- **DAO instrumented tests** run on every push to `main` and on pull requests targeting `main` using a managed x86_64 emulator via `reactivecircus/android-emulator-runner`.

See `.github/workflows/` for the workflow definitions.

---

## Further reading

- `AGENT_GUIDE.md` in this repo -- detailed architecture rules and coding standards.
- [Guide to app architecture](https://developer.android.com/topic/architecture) -- Google's
  official architecture guidance (the patterns in this project are closely aligned).
- [Hilt documentation](https://dagger.dev/hilt/) -- dependency injection reference.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) -- the UI toolkit used here.
