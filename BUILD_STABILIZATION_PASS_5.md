# BUILD STABILIZATION PASS 5

## Root Cause Analysis

### Compose BOM 2024.01.00 — Material3 1.1.2 Compatibility

The codebase was using APIs introduced in Material3 **1.2.0** while the project's Compose BOM (`2024.01.00`) resolves Material3 to **1.1.2**. This caused all compilation failures.

| API | Material3 Version | Replacement |
|-----|-------------------|-------------|
| `HorizontalDivider` | 1.2.0+ | `Divider` |
| `FilterChipDefaults.filterChipBorder(enabled, selected)` | 1.2.0+ | Remove `enabled`/`selected` params |
| `contentColorFor` | 1.2.0+ | Remove unused import |

### Icon API Mismatches

| Icon | Issue | Fix |
|------|-------|-----|
| `Icons.AutoMirrored.Filled.ArrowUpward` | `ArrowUpward` is not in `AutoMirrored` set | `Icons.Filled.ArrowUpward` |
| `Icons.AutoMirrored.Filled.History` | `History` is not in `AutoMirrored` set | `Icons.Filled.History` |

### Kotlin API Mismatch

`String.removePrefix(Regex)` — `Regex` is not a `CharSequence`. Replaced with `String.replaceFirst(Regex, "")`.

### Missing Imports

- `ActivityViewModel.kt` — missing `import androidx.lifecycle.viewModelScope`
- `ActionsDashboardScreen.kt` — missing `import com.repoflow.core.domain.model.Workflow`

---

## Files Changed (17 files, +40/-39 lines)

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

### GLOBAL SCAN — HorizontalDivider → Divider across entire codebase

| File | Changes |
|------|---------|
| `AccountScreen.kt` | Import + usage |
| `SettingsScreen.kt` | Import + usage |
| `RepositoryDetailScreen.kt` | Import + usage |
| `GitStatusScreen.kt` | Import + usage |
| `IssueDetailScreen.kt` | Import + usage |
| `PullRequestDetailScreen.kt` | Import + 2 usages |
| `PcBridgeRemoteScreen.kt` | Import only (no usage of `HorizontalDivider`) |
| `GitErrorHelpScreen.kt` | Import + 2 usages |
| `ConflictHelpScreen.kt` | Import + usage |
| `RepoSummaryScreen.kt` | Import + 2 usages |

---

## Imports Added/Removed

### Added
- `import androidx.lifecycle.viewModelScope` — `ActivityViewModel.kt`
- `import com.repoflow.core.domain.model.Workflow` — `ActionsDashboardScreen.kt`

### Removed
- `import androidx.compose.material3.contentColorFor` — `CommitScreen.kt`

### Changed globally
- `import androidx.compose.material3.HorizontalDivider` → `import androidx.compose.material3.Divider` (10 files)

---

## Dependencies Added

None. All required dependencies already present:
- `libs.androidx.material3` (via Compose BOM) — provides `Divider`
- `libs.androidx.material.icons.extended` — provides all icon variants
- `libs.androidx.lifecycle.viewmodel.compose` — provides `viewModelScope`

---

## Remaining Expected Blockers

| Issue | Status | Notes |
|-------|--------|-------|
| `HorizontalDivider` unresolved | **RESOLVED** — Replaced with `Divider` in all 17 files |
| `ArrowUpward` unresolved | **RESOLVED** — Changed to `Icons.Filled.ArrowUpward` |
| `History` icon unresolved | **RESOLVED** — Changed to `Icons.Filled.History` |
| `Workflow` unresolved | **RESOLVED** — Added import for domain model `Workflow` |
| `filterChipBorder` `enabled`/`selected` | **RESOLVED** — Removed from call |
| `viewModelScope` unresolved | **RESOLVED** — Added import to `ActivityViewModel.kt` |
| `contentColorFor` unresolved | **RESOLVED** — Removed unused import |
| `Regex` vs `CharSequence` | **RESOLVED** — Changed to `String.replaceFirst(Regex, "")` |
| `replaceFirstChar` errors | **No action needed** — API exists in Kotlin 1.9.22 |
| `Alignment.CenterVertically` misuse | **Not found** — All usages are correct |
| `Icons.Filled.Sync` receiver mismatch | **Not confirmed** — Import is correct; icon exists in extended library |
| Smart cast impossible | **Not confirmed** — `WorkflowJob.conclusion` is `val` so smart cast works |
| Composable invocation outside composable | **Not confirmed** — All composable calls are in `@Composable` scope |
| Suspend function called outside coroutine | **Not confirmed** — All `suspend` calls are in `viewModelScope.launch` |

---

## Success Criteria

- [x] All known Material3 1.2.0+ APIs replaced with Material3 1.1.2 compatible equivalents
- [x] All icon path errors fixed
- [x] All missing imports added
- [x] Regex/removePrefix type mismatch fixed
- [ ] `compileDebugKotlin` — **requires CI build** (no local gradlew)
- [ ] `compileReleaseKotlin` — **requires CI build**
- [ ] `assembleDebug` — **requires CI build** (needs keystore for release)
- [ ] `assembleRelease` — **requires CI build**
