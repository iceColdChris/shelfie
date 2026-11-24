# Conventions

- Use case: one class, `@Inject constructor(repository)`, `operator fun invoke`, one-line KDoc. Named `Get*`/`Add*`/`Toggle*`... Even pass-throughs exist; ViewModels never skip them.
- Repository: interface (public, KDoc per method) + `*Impl` (internal) in `core/data/repository`; reads are `Flow`, writes `suspend`. Bound unscoped in `DataModule` (stateless).
- Mappers: `internal object XMapper` with extension functions, imported as `XMapper.toDomain`.
- Hilt modules are `internal`, in a `di` package, `SingletonComponent` only.
- UI: one `StateFlow<UiState>` per ViewModel; `UiState` is an `@Immutable data class` with `ImmutableList`; `UiEvent` sealed interface; `XScreen` (stateful) + internal `XContent(state, onEvent)`.
- Models/entities never cross: DTO/Entity -> model in `core:data` only.
- ASCII only in identifiers and comments.
- Tests: backtick names, MockK (`mockk(relaxed = true)` for write use cases), `MainDispatcherRule` from `core:testing`, test sits in the module of the class under test.
- Module `build.gradle.kts`: one convention plugin + `namespace` + module-specific deps; no SDK/Java settings.
