# Android Agent Architecture Guide

## High-Level Mission
Build Shelfie, a scalable, enterprise-grade Android book tracking application using Multi-Module Clean Architecture. Prioritize Type Safety, Offline-First Data Flow, and Unidirectional Data Flow (UDF).

---

## Build Toolchain (AGP 9.x)
This project uses Android Gradle Plugin 9.1.0+ with Gradle 9.3.1+.

### Key AGP 9 rules
* Built-in Kotlin: AGP 9 bundles the Kotlin Gradle Plugin. Do NOT apply `org.jetbrains.kotlin.android` -- it will conflict.
* Compose Compiler: The `org.jetbrains.kotlin.plugin.compose` plugin IS still required for Compose modules.
* kotlinOptions removed: Use a top-level `kotlin { compilerOptions { } }` block instead of `android { kotlinOptions { } }`.
* Bundled KGP version: AGP 9.1.0 ships KGP 2.2.10. Applying a Kotlin plugin (e.g. kotlin-compose) with a higher version will override the bundled version project-wide.
* KSP alignment: AGP auto-upgrades KSP below 2.2.10-2.0.2. Set KSP to match the active Kotlin version.
* Hilt Gradle Plugin: Requires Dagger 2.59+ for AGP 9 compatibility (earlier versions reference the removed BaseExtension API).
* New DSL: `BaseExtension`, `applicationVariants`, and the old variant API are removed. Use `androidComponents { }` instead.

---

## Project Structure
Every new feature must be encapsulated in its own Gradle module: :feature:[name].

### 1. Global Core Modules
* :app -- Root entry point. Contains NavHost, @HiltAndroidApp Application, and @AndroidEntryPoint Activity.
* :core:ui -- Design System, Theme.kt, and shared @Composable components.
* :core:data -- Shared persistence and networking logic (Room AppDatabase, Hilt NetworkModule/DatabaseModule, Entities, DAOs).

### 2. Where data lives (avoiding circular deps)
* Room AppDatabase, shared Entities, DAOs, and TypeConverters belong in :core:data because feature modules cannot see each other.
* Feature-specific Retrofit interfaces (e.g. OpenLibraryApi) belong in the feature module under data/remote.
* Feature modules depend on :core:data to receive DAOs and Retrofit via Hilt.

### 3. Feature Internal Blueprint
When creating a feature, strictly use this package hierarchy:

com.shelfie.feature.[name]
├── data
│   ├── remote          # Retrofit interface + DTOs
│   ├── mapper          # Internal mappers (Dto -> Domain, Entity -> Domain)
│   └── repository      # Internal Implementation of the Repository
├── domain
│   ├── model           # Pure Kotlin POJOs (The truth used by UI)
│   ├── repository      # Repository Interface
│   └── usecase         # Single-purpose business logic (e.g., GetTasksUseCase)
├── di                  # Hilt Module (@InstallIn(ViewModelComponent::class))
└── ui
    ├── [Name]Screen.kt     # Stateless Entry Composable
    ├── [Name]ViewModel.kt  # Hilt ViewModel (Uses UseCases)
    ├── [Name]UiState.kt    # Immutable State data class
    └── [Name]UiEvent.kt    # Sealed class for user actions (Clicks, Inputs)

---

## Coding Standards

### 1. The Internal Rule
* Within a feature module: Repository Implementations, Mappers, Retrofit interfaces, and Hilt Modules must be internal.
* Within :core:data: The AppDatabase and Hilt Modules (NetworkModule, DatabaseModule) are internal. DAOs and Entities must be public because they are injected into feature modules across Gradle module boundaries via Hilt.
* Public API surface: Repository Interfaces, Use Cases, Domain Models, DAOs, and Entities.

### 2. UI & State (Compose)
* UDF Only: The ViewModel exposes exactly one StateFlow<UiState>.
* Statelessness: Every screen must have a Content Composable that takes UiState and a (UiEvent) -> Unit lambda.
* Modifiers: Every Composable should accept a Modifier as its first optional parameter.

### 3. Data Integrity
* Offline-First: Use Room as the single source of truth. The Repository should observe a Local Flow and refresh from Network in the background.
* Mapping: Never pass a Network DTO or Database Entity to a Composable. Map them to a Domain Model in the data layer.

### 4. Dependency Injection (Hilt)
* Always use @Inject constructor for class dependencies.
* Use @ViewModelScoped for dependencies that only live within a feature screen.
* Provide interfaces using Hilt @Binds in the di package.
* Singleton-scoped providers (Retrofit, Room, OkHttp, Moshi) go in :core:data Hilt modules with @InstallIn(SingletonComponent::class).
* Feature-scoped providers (API interfaces, repository bindings) go in the feature's di package with @InstallIn(ViewModelComponent::class).

---

## Documentation & Testing
* KDoc: Add a brief summary for every UseCase and Repository interface.
* Testing Requirements:
    * Unit Tests: Required for ViewModels (state transitions) and UseCases (logic).
    * Integration Tests: Required for Room DAOs using an in-memory database.
    * Mocks: Use MockK for unit testing dependencies.

---

## Warp Agent Command Instructions
1. Context Check: Before generating code, check for existing themes in :core:ui.
2. Boilerplate First: Always start with the domain layer to define the contract before implementing data or UI.
3. No Short-circuiting: Do not bypass UseCases to call Repositories directly from the ViewModel.
4. Character Set: Use standard ASCII characters in code identifiers and comments. Avoid fancy quotes or em-dashes. Unicode is acceptable in user-facing string resources.
