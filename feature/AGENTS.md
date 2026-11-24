# Feature modules

- Files in `com.shelfie.feature.<name>`: `XScreen`, `XViewModel`, `XUiState`, `XUiEvent`.
- ViewModel exposes exactly one `StateFlow<UiState>`. `UiState` is an `@Immutable data class`; lists are `ImmutableList`.
- `XScreen` gets the ViewModel and wires callbacks; `internal XContent(state, onEvent, modifier)` renders any state without a ViewModel.
- User actions: `sealed interface XUiEvent`, handled by one `onEvent`.
- `Modifier` is the first optional parameter of every composable.
- Style with `:core:designsystem` theme tokens (`ShelfieTheme`, `MaterialTheme.colorScheme`, typography). A model-agnostic component two features need goes in `:core:designsystem`; one that takes a `:core:model` type goes in a new `:core:ui`.
- WCAG 2.2 AA: every interactive element has a text label or `contentDescription`; custom controls set `stateDescription`.
- `hiltViewModel` comes from `androidx.hilt.lifecycle.viewmodel.compose`.
- Known exceptions; leave them unless the task targets them: `StatsScreen` has no `UiEvent`; `BookDetailScreen` calls ViewModel methods directly, its `BookDetailContent` is `private`, and its `viewModel` parameter is required (see `app/AGENTS.md`).

## New feature module
1. Use cases in `:core:domain`, repository and mapper in `:core:data` first.
2. `feature/<name>/build.gradle.kts`: `shelfie.android.feature` + namespace `com.shelfie.feature.<name>`; `include` it in `settings.gradle.kts`.
3. In `:app`: dependency, `*NavKey`, `entry`, and a `TopLevelNavItem` entry if it is a tab.

Done when it depends on no other feature and `:app:assembleDebug` plus its ViewModel tests pass.
