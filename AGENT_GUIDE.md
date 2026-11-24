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
A feature module holds UI only; data and business logic live in the core modules.

### 1. Modules and allowed dependencies
* :app -- Root entry point. Contains NavHost, @HiltAndroidApp Application, and @AndroidEntryPoint Activity. Depends on every module.
* :feature:[name] -- Screens and ViewModels. Depends on :core:domain, :core:model, :core:ui. Never on another feature, :core:data, :core:database or :core:network.
* :core:domain -- Use cases. Depends on :core:data (implementation) and :core:model (api).
* :core:data -- Repository interfaces, implementations, mappers. Depends on :core:database, :core:network (implementation) and :core:model (api).
* :core:database -- Room AppDatabase, Entities, DAOs, TypeConverters, DatabaseModule. Depends on no project module.
* :core:network -- Retrofit/OkHttp/Moshi NetworkModule, Retrofit interfaces (e.g. OpenLibraryApi), DTOs. Depends on no project module.
* :core:model -- Pure Kotlin domain models. No Android, depends on no project module.
* :core:ui -- Design System, Theme.kt, and shared @Composable components.
* :core:testing -- Shared test rules and fakes (pure Kotlin).

### 2. Build configuration
* Every module applies one convention plugin from build-logic/convention (shelfie.android.feature, shelfie.android.library, shelfie.android.library.compose, shelfie.android.hilt, shelfie.jvm.library, shelfie.android.application) and sets its namespace.
* Do not set compileSdk, minSdk or Java versions in a module; they are set once in shelfie.android.library.
* A new Gradle plugin is added to build-logic/convention/build.gradle.kts and applied inside a convention, never with a version in a module.
* :app must depend on every module that has a Hilt module or an @Inject constructor.

### 3. Package Blueprint
When creating a feature, strictly use this package hierarchy:

com.shelfie.feature.[name].ui
├── [Name]Screen.kt     # Stateless Entry Composable
├── [Name]ViewModel.kt  # Hilt ViewModel (Uses UseCases)
├── [Name]UiState.kt    # Immutable State data class
└── [Name]UiEvent.kt    # Sealed class for user actions (Clicks, Inputs)

The layers a feature needs go in the core modules:

com.shelfie.core.model              # Pure Kotlin models (The truth used by UI)
com.shelfie.core.domain             # Single-purpose business logic (e.g., GetShelfBooksUseCase)
com.shelfie.core.data.repository    # Repository Interface + internal Implementation
com.shelfie.core.data.mapper        # Internal mappers (Dto -> Model, Entity -> Model)
com.shelfie.core.data.di            # Hilt Module binding interface to implementation
com.shelfie.core.database           # dao, entity, converter, di
com.shelfie.core.network            # Retrofit interface, dto, di

---

## Coding Standards

### 1. The Internal Rule
* Within :core:data: Repository Implementations, Mappers, and the Hilt Module must be internal.
* Within :core:database and :core:network: The AppDatabase and Hilt Modules (NetworkModule, DatabaseModule) are internal. DAOs, Entities, Retrofit interfaces and DTOs are public because :core:data uses them across a Gradle module boundary; no other module may depend on these two.
* Public API surface seen by features: Use Cases and Models only.

### 2. UI & State (Compose)
* UDF Only: The ViewModel exposes exactly one StateFlow<UiState>.
* Statelessness: Every screen must have a Content Composable that takes UiState and a (UiEvent) -> Unit lambda.
* Modifiers: Every Composable should accept a Modifier as its first optional parameter.

### 3. Data Integrity
* Offline-First: Use Room as the single source of truth. The Repository should observe a Local Flow and refresh from Network in the background.
* Mapping: Never pass a Network DTO or Database Entity to a Composable. Map them to a Domain Model in the data layer.

### 4. Dependency Injection (Hilt)
* Always use @Inject constructor for class dependencies.
* Provide interfaces using Hilt @Binds in the di package.
* Singleton-scoped providers (Retrofit and API interfaces, OkHttp, Moshi, Room) go in the :core:network and :core:database Hilt modules with @InstallIn(SingletonComponent::class).
* Repository bindings go in :core:data's DataModule with @InstallIn(SingletonComponent::class), unscoped: repositories hold no state.
* Feature modules have no Hilt modules.

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
