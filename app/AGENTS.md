# :app - Navigation 3

- Nav keys are `@Serializable` `NavKey`s named `*NavKey`, in `src/main/kotlin/com/shelfie/app/navigation/NavKeys.kt`; serializable keeps back stacks across process death.
- One back stack per tab: `NavigationState` + `Navigator` (adapted from Google's Navigation 3 migration guide; names follow NiA's `core/navigation`). Back at a tab root returns to the start tab.
- Destination = `entry<XNavKey>` in `appEntryProvider` (`MainActivity.kt`). A tab is also a `TopLevelNavItem` entry.
- Nav key arguments reach a ViewModel through Hilt assisted injection (pattern: `BookDetailViewModel`): `@HiltViewModel(assistedFactory = X.Factory::class)`, `@AssistedInject constructor(@Assisted ...)`, and in the entry `hiltViewModel<X, X.Factory>(creationCallback = { it.create(key.arg) })`.
- That screen takes `viewModel` as a required parameter: a default `hiltViewModel()` cannot supply the argument and crashes at runtime.
- Keep `rememberViewModelStoreNavEntryDecorator()` in `NavigationState.toEntries`. Without it entries share one ViewModel and a second book shows the first one's data.
