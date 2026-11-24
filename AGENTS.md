# Shelfie agent rules

Rules not inferable from the build files. Read the subsystem file before touching that area:
- Gradle files, plugins, versions, SDK levels: `build-logic/AGENTS.md`
- Navigation 3, routes, ViewModel arguments: `app/AGENTS.md`
- Screens, ViewModels, UI state, new feature modules: `feature/AGENTS.md`
- Room entities, DAOs, schema, instrumented tests: `core/database/AGENTS.md`

## Boundaries
- Feature modules depend only on `:core:domain`, `:core:model`, `:core:designsystem`; never on another feature or a data module.
- `:core:domain` takes `:core:data` as `implementation` on purpose: repositories stay off feature classpaths, so ViewModels call use cases only.
- Every data operation a ViewModel needs gets a use case in `:core:domain`, even a pass-through.
- Network models (`Network*`) and entities (`*Entity`) become models only in `:core:data` mappers; composables receive `:core:model` types.
- `:app` owns every `NavKey`; features expose plain callbacks.
- `:app` depends on every module with a Hilt module or `@Inject` constructor. A missing one fails only `:app:assembleDebug`, so run it after any DI or module change.
- Hilt modules exist only in `:core:data`, `:core:database`, `:core:network`, all `@InstallIn(SingletonComponent::class)`; repository bindings stay unscoped.
- `:core:model` and `:core:testing` are JVM modules: no Android APIs. `@Immutable` comes from Compose `runtime-annotation`.

## Visibility
- `internal`: repository implementations, mappers, `ShelfieDatabase`, `StringListConverter`, every Hilt module.
- Public only for `:core:data`'s use across a module boundary: DAOs, entities, `OpenLibraryApi`, network models.

## Naming (Now in Android conventions)
- Repository impl: `OfflineFirst*Repository` when it syncs network into Room, `Default*Repository` when it reads local data only.
- Room: `*Entity` and `*Dao` in `core.database.model` / `.dao`; network types `Network*` in `core.network.model`; converters named for what they convert.
- Nav keys `*NavKey`; tabs `TopLevelNavItem`; feature packages `com.shelfie.feature.<name>` (no `.ui`); sources in `src/*/kotlin`.
- `:core:designsystem` holds theme only. Create `:core:ui` when a composable taking a `:core:model` type is shared by two features.

## Code
- Offline-first: repositories read a Room `Flow` and refresh from the network into Room. Book detail is the one network-only read.
- In a `try`, rethrow `CancellationException` before `catch (e: Exception)`.
- ASCII in identifiers and comments; Unicode only in user-facing strings.

## Before finishing
- Touched navigation: there are no navigation tests. Run the app; check tab switching, back from a tab root, opening two different books.
- Changed a rule, module, command or toolchain: update the matching `AGENTS.md`, `README.md`, and `.github/workflows/`.
