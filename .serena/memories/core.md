# Shelfie core

Android book tracker. Multi-module, Google modularization layout. Authoritative rules: `AGENT_GUIDE.md` (repo root); README mirrors it.

## Module graph (one direction only)
- `app` -> everything (must list every module with a Hilt module or @Inject constructor)
- `feature/{books,readinglist,stats}`: UI + ViewModel only, package `com.shelfie.feature.<name>.ui`. Deps: `core:domain`, `core:model`, `core:ui`. No Hilt modules.
- `core/domain` (use cases, flat package `com.shelfie.core.domain`) -> `core:data` as `implementation` on purpose: repositories are not on a feature's classpath.
- `core/data` (`repository/` interfaces + internal impls, `mapper/`, `di/DataModule`) -> `core:database`, `core:network`
- `core/database` (Room), `core/network` (Retrofit, OpenLibraryApi, DTOs): no project deps
- `core/model`, `core/testing`: pure JVM modules
- `build-logic/convention`: precompiled script plugins `shelfie.*`

## Invariants
- ViewModels call use cases only, never a repository or DAO (Gradle enforces it).
- Navigation routes are strings in `app/MainActivity.kt`; `BookDetailViewModel` reads `savedStateHandle["bookId"]`. The key is duplicated across modules; change both.
- `Shelf.value` is the string stored in Room; `Shelf.fromValue` throws on unknown values.

Further: build/toolchain gotchas `mem:tech_stack`; commands `mem:suggested_commands`; style `mem:conventions`; done-checklist `mem:task_completion`.
