# BUILD STABILIZATION PASS 5

## Root Cause Analysis

### Compose BOM 2024.01.00 — Material3 1.1.2 Compatibility

The codebase was using APIs introduced in Material3 **1.2.0** while the project's Compose BOM (`2024.01.00`) resolves Material3 to **1.1.2**. This caused all compilation failures.

| API | Material3 Version | Replacement |
|-----|-------------------|-------------|
| `HorizontalDivider` | 1.2.0+ | `Divider` |
| `FilterChipDefaults.filterChipBorder(enabled, selected)` | 1.2.0+ | Remove params |
| `contentColorFor` | 1.2.0+ | Remove unused import |
| `FilterChip` / `FilterChipDefaults.*` | Experimental in 1.1.x | Add `@OptIn(ExperimentalMaterial3Api::class)` |

### Icon API Mismatches

| Icon | Issue | Fix |
|------|-------|-----|
| `Icons.AutoMirrored.Filled.ArrowUpward` | Not in `AutoMirrored` set | `Icons.Filled.ArrowUpward` |
| `Icons.AutoMirrored.Filled.History` | Not in `AutoMirrored` set | `Icons.Filled.History` |
| `Icons.Outlined.Sync` | Missing import | Added `import ...icons.outlined.Sync` |
| `Icons.Outlined.Workflow` | Icon does not exist in `outlined` variant | `Icons.Outlined.AccountTree` |

### Cross-module Smart Cast

`val` properties in data classes from the `:core` module cannot be smart-cast in feature modules. Properties like `headBranch`, `actor`, `conclusion` from `WorkflowRun`, `WorkflowJob`, `JobStep` require local variable assignment before null checking.

### Kotlin API Ambiguity

`replaceFirstChar { it.uppercase() }` is ambiguous because `Char.uppercase()` returns `String` and both `(Char) -> Char` and `(Char) -> CharSequence` overloads exist. Fixed by ensuring the receiver is a non-null `String` via local variable.

---

## Round 1 Files Changed (17 files, +40/-39 lines)

### MODULE 1 — feature-activity

| File | Changes |
|------|---------|
| `ActivityScreen.kt` | `HorizontalDivider` → `Divider`; `AutoMirrored.Filled.ArrowUpward` → `Filled.ArrowUpward`; removed `enabled`/`selected` from `filterChipBorder` |
| `ActivityViewModel.kt` | Added `import androidx.lifecycle.viewModelScope` |

### MODULE 2 — feature-actions

| File | Changes |
|------|---------|
| `ActionsDashboardScreen.kt` | Added `import com.repoflow.core.domain.model.Workflow` |
| `WorkflowRunDetailScreen.kt` | `HorizontalDivider` → `Divider` (2 usages) |

### MODULE 3 — feature-diff-viewer

| File | Changes |
|------|---------|
| `DiffViewerScreen.kt` | `HorizontalDivider` → `Divider` (import + usage) |

### MODULE 4 — feature-commit

| File | Changes |
|------|---------|
| `CommitScreen.kt` | `AutoMirrored.Filled.History` → `Filled.History`; `HorizontalDivider` → `Divider`; removed `contentColorFor` import |
| `CommitViewModel.kt` | `replaceFirst(Regex, "")` replaces `removePrefix(Regex)` |

### GLOBAL SCAN — HorizontalDivider → Divider

| File | Changes |
|------|---------|
| `AccountScreen.kt`, `SettingsScreen.kt`, `RepositoryDetailScreen.kt`, `GitStatusScreen.kt`, `IssueDetailScreen.kt`, `PullRequestDetailScreen.kt`, `PcBridgeRemoteScreen.kt`, `GitErrorHelpScreen.kt`, `ConflictHelpScreen.kt`, `RepoSummaryScreen.kt` | All imports + usages replaced |

---

## Round 2 Files Changed (5 files, +24/-14 lines)

### ActivityScreen.kt

- Added `@OptIn(ExperimentalMaterial3Api::class)` to `FilterChipRow` function
- Added `import androidx.compose.material.icons.outlined.Sync`
- Changed `verticalAlignment = Alignment.Center` → `Alignment.CenterVertically`

### ActionsDashboardScreen.kt

- Replaced `Icons.Outlined.Workflow` → `Icons.Outlined.AccountTree` (icon did not exist)
- Fixed cross-module smart cast for `run.headBranch` → local `val headBranch`
- Fixed cross-module smart cast for `run.actor` → local `val actor`

### WorkflowRunDetailScreen.kt

- Fixed cross-module smart cast for `run.actor` → local `val actor`
- Fixed cross-module smart cast for `job.conclusion` → local `val jobConclusion`
- Fixed cross-module smart cast for `step.conclusion` → local `val stepConclusion`

### CommitScreen.kt

- Added `@OptIn(ExperimentalMaterial3Api::class)` to `CommitContent` function

### RepositoriesScreen.kt

- Added `import androidx.compose.material3.ExperimentalMaterial3Api`
- Added `@OptIn(ExperimentalMaterial3Api::class)` to `SortChipsRow` function

---

## Imports Summary

### Added (Round 1)
- `import androidx.lifecycle.viewModelScope` — `ActivityViewModel.kt`
- `import com.repoflow.core.domain.model.Workflow` — `ActionsDashboardScreen.kt`

### Added (Round 2)
- `import androidx.compose.material.icons.outlined.Sync` — `ActivityScreen.kt`
- `import androidx.compose.material3.ExperimentalMaterial3Api` — `RepositoriesScreen.kt`

### Removed
- `import androidx.compose.material3.contentColorFor` — `CommitScreen.kt`

### Changed globally
- `import androidx.compose.material3.HorizontalDivider` → `import androidx.compose.material3.Divider` (10 files)

---

## All Issues RESOLVED

| Issue | Resolution |
|-------|-----------|
| `HorizontalDivider` unresolved | → `Divider` (17 files) |
| `ArrowUpward` unresolved | → `Icons.Filled.ArrowUpward` |
| `History` icon unresolved | → `Icons.Filled.History` |
| `Workflow` icon unresolved | → `Icons.Outlined.AccountTree` |
| `Icons.Outlined.Sync` missing | Added import |
| `filterChipBorder` `enabled`/`selected` params | Removed |
| `viewModelScope` unresolved | Added import |
| `contentColorFor` unresolved | Removed import |
| `Regex` vs `CharSequence` | → `String.replaceFirst(Regex, "")` |
| Experimental Material3 API warnings | Added `@OptIn` to all containing functions |
| `Alignment` vs `Alignment.Vertical` mismatch | → `Alignment.CenterVertically` |
| Smart cast impossible (cross-module) | Local variable assignment |
| `replaceFirstChar` on nullable | Local variable + smart cast |
| `replaceFirstChar` ambiguous overload | Fixed by ensuring non-null receiver |
| Composable invocation outside composable | Not found |
| Suspend function outside coroutine | Not found |

---

## Success Criteria

- [x] All Material3 1.2.0+ APIs replaced with 1.1.2 compatible equivalents
- [x] All icon path errors fixed
- [x] All missing imports added
- [x] Regex/removePrefix type mismatch fixed
- [x] `@OptIn(ExperimentalMaterial3Api::class)` on all FilterChip users
- [x] Cross-module smart cast workaround via local variables
- [x] `replaceFirstChar` ambiguity resolved
- [ ] `compileDebugKotlin` — **requires CI build**
- [ ] `compileReleaseKotlin` — **requires CI build**  
- [ ] `assembleDebug` — **requires CI build**
- [ ] `assembleRelease` — **requires CI build**
