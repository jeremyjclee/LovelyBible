# PLAN_v1.0.6-line width

## Goal Description
Implement several UI/UX improvements and fixes:
1.  **Defaults**: Change default font size to level 2.
2.  **PPT Window Stability**: Fix the issue where PPT window disappears/closes when clicking the main app.
3.  **Search Improvements**:
    *   Add Initial Consonant (Cho-sung) search support.
    *   Add Keyboard navigation (Up/Down/Enter) for search candidates.
    *   Disable auto-confirm for Cho-sung single matches.
4.  **Configuration**: Add "Max Line Width" setting (Default 1120dp, Range 0-1920).

## User Review Required
> [!IMPORTANT]
> **PPT Window Focus Change**: To prevent the PPT window from disappearing when interaction occurs in the main app, we will set `focusable = false` for the Presentation Window. This treats it as a pure display output. This means **keyboard events (like Escape to close) directly on the PPT window might not work** if it cannot accept focus. We rely on the Main Window controls or global shortcuts.
>
> **Cho-sung Auto-Confirm**: As requested, auto-confirm is strictly disabled for Cho-sung matches even if there is only 1 candidate.

## Proposed Changes

### Phase 1: Configuration & Display Stability
**Goal**: Implement Line Width settings, fix default font size, and resolve PPT window focus issues.

#### [MODIFY] [SettingsRepository.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/domain/repository/SettingsRepository.kt)
- Add `maxLineWidth` property (get/set).

#### [MODIFY] [JvmSettingsRepository.kt](file:///sample/composeApp/src/jvmMain/kotlin/com/lovelybible/data/repository/JvmSettingsRepository.kt)
- Implement `maxLineWidth` using Preferences.
- Default value: 1120.

#### [MODIFY] [SettingsState.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/settings/SettingsState.kt)
- Add `maxLineWidth: Int`.
- Update `PresentationState` to include `maxLineWidth`.

#### [MODIFY] [SettingsDialog.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/settings/SettingsDialog.kt)
- Add specific UI for "Max Line Width".
- Input field with number restriction.
- Validation logic: Range 0 ~ 1920. Error message if out of range.

#### [MODIFY] [PresentationWindow.kt](file:///sample/composeApp/src/jvmMain/kotlin/com/lovelybible/feature/presentation/PresentationWindow.kt)
- Set `focusable = false` to prevent window management conflicts.

#### [MODIFY] [PresentationContent.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/presentation/PresentationContent.kt)
- Use the injected `maxLineWidth` instead of hardcoded 1300dp.
- Change default font level from 4 to 2.

---

### Phase 2: Search Logic Core (Cho-sung)
**Goal**: Implement robust Cho-sung matching logic and unit tests.

#### [MODIFY] [BibleBookNames.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/domain/model/BibleBookNames.kt)
- Implement `matchChoSung(text, query)`:
    - Decompose text into Cho-sungs (e.g., "창세기" -> "ㅊㅅㄱ").
    - Support "Starts With" and "Contains".

#### [NEW] [BibleBookNamesTest.kt](file:///sample/composeApp/src/commonTest/kotlin/com/lovelybible/domain/model/BibleBookNamesTest.kt)
- Unit tests for Cho-sung matching scenarios.
- Cases: "ㅊ", "ㅊㅅ", "ㅊㅅㄱ", "ㄱㄹㄷ"(고린도), etc.

#### [MODIFY] [BibleRepository.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/domain/repository/BibleRepository.kt)
- Update `searchBooks` to use the new Cho-sung matching logic.

---

### Phase 3: Search UI & Interaction
**Goal**: Implement keyboard navigation for candidates and refined selection logic.

#### [MODIFY] [SearchState.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/search/SearchState.kt)
- Add `focusedSuggestionIndex: Int` (default -1).
- Add `isChoSungMatch: Boolean` (to track if current suggestions were matched via Cho-sung).

#### [MODIFY] [SearchViewModel.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/search/SearchViewModel.kt)
- Handle `SearchIntent.MoveSuggestionFocus(Direction)`.
- Handle `SearchIntent.SelectFocusedSuggestion`.
- Update `searchBooks` logic to flag `isChoSungMatch`.
- In `updateBookQuery`, prevent auto-selection if `isChoSungMatch` is true.

#### [MODIFY] [SearchPanel.kt](file:///sample/composeApp/src/commonMain/kotlin/com/lovelybible/feature/search/SearchPanel.kt)
- In `BookSearchField`:
    - Listen for `Key.DirectionUp` and `Key.DirectionDown`.
    - Pass events to ViewModel.
    - Highlight the `focusedSuggestionIndex` item with a purple border.
    - Handle `Key.Enter` to select the focused item.

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `BibleBookNamesTest`: Verify Cho-sung matching logic.
    - `SettingsRepositoryTest`: Verify default values and persistence.
    - `SearchViewModelTest`: Verify keyboard navigation state changes and auto-select suppression.

### Manual Verification
1.  **PPT Window**:
    - Open PPT Mode on secondary monitor.
    - Click around in the Main App.
    - **Verify**: PPT window stays open and visible.
2.  **Settings**:
    - Go to Settings. Verify "Max Line Width" is 1120.
    - Try entering 2000 -> Should block/warn.
    - Enter 1000 -> Save.
    - Re-open Settings -> Verify 1000 is saved.
    - Open PPT -> Verify line width constraint change.
3.  **Cho-sung Search**:
    - Type "ㅊㅅㄱ".
    - **Verify**: "창세기" appears in candidates.
    - **Verify**: It does NOT auto-select even if it's the only one.
4.  **Keyboard Nav**:
    - Type "ㅁ". (Multiple candidates: 마태, 마가, etc.)
    - Press Down Arrow -> First item highlighted (Purple border).
    - Press Down Arrow -> Second item highlighted.
    - Press Enter -> Item selected, focus moves to Chapter field.
