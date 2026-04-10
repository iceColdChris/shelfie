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
:app                  -- Entry point: Application class, single Activity, NavHost
:core:ui              -- Shared design system (theme, colors, typography, reusable composables)
:core:data            -- Shared persistence & networking (Room database, Retrofit, Hilt modules)
:feature:books        -- Book search & favorites
:feature:readinglist  -- Personal reading shelf management
:feature:stats        -- Aggregated reading statistics
```

Each feature module is self-contained and follows the same internal package layout
described below.

---

## Feature module blueprint

Every feature follows this exact package structure:

```
com.shelfie.feature.<name>
├── data
│   ├── remote/          Retrofit API interface + DTO classes
│   ├── mapper/          Functions that convert DTOs/Entities -> Domain models
│   └── repository/      Repository *implementation* (always `internal`)
├── domain
│   ├── model/           Pure Kotlin data classes -- the "truth" the UI works with
│   ├── repository/      Repository *interface* (public contract)
│   └── usecase/         Single-purpose business-logic classes
├── di/                  Hilt module that wires everything together
└── ui/
    ├── <Name>Screen.kt       Stateless @Composable entry point
    ├── <Name>ViewModel.kt    Hilt ViewModel -- owns the state
    ├── <Name>UiState.kt      Immutable data class describing what the screen shows
    └── <Name>UiEvent.kt      Sealed interface for every user action
```

### Why this matters

Android newcomers often put everything in one package. Separating layers gives you:

- **Testability** -- you can unit-test a UseCase without spinning up a real database.
- **Compile isolation** -- changing UI code does not recompile the data layer.
- **Enforced boundaries** -- the `internal` keyword prevents other modules from reaching
  into implementation details.

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

- Repository **implementations**, mappers, Retrofit interfaces, and Hilt modules are `internal`.
- Repository **interfaces**, UseCases, and domain models are **public** because other modules
  (like `:app`) need to see them.

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
- **`@InstallIn(SingletonComponent::class)`** for app-wide singletons (Retrofit, Room).
- **`@InstallIn(ViewModelComponent::class)`** for feature-scoped bindings (repositories, APIs).
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
| Unit (JVM) | JUnit 4 + MockK + Turbine | Business logic in isolation | `src/test/` in each feature module |
| DAO (instrumented) | JUnit 4 + Room in-memory | Real SQL queries and Flow reactivity | `core/data/src/androidTest/` |

> **JUnit 5** has no native Android support for instrumented tests and requires a third-party Gradle plugin for JVM-only tests. Given the project is stable on JUnit 4 (`4.13.2`, the current latest), migrating adds complexity with no meaningful benefit.

### Running unit tests

```bash
# All modules at once
./gradlew test

# A single module
./gradlew :feature:books:testDebugUnitTest
```

This covers **115 tests** across 14 classes:
- `feature:books` — `BookMapper`, `BookRepositoryImpl`, `BooksViewModel`, `BookDetailViewModel`, four use case tests
- `feature:readinglist` — `ReadingListMapper`, `ReadingListRepositoryImpl`, `ReadingListViewModel`, `UpdateProgressUseCase`
- `feature:stats` — `StatsRepositoryImpl` (streak + monthly chart algorithms), `StatsViewModel`

All ViewModel tests use a shared `MainDispatcherRule` (a JUnit4 `TestWatcher`) instead of manual `@Before/@After` dispatcher setup.

### Running DAO instrumented tests

A connected device or emulator is required.

```bash
./gradlew :core:data:connectedDebugAndroidTest
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
