# PLAN_v1.0.0-font size control

# Font Size Control & UI Refinement

This plan addresses the request to implement dynamic font size control (1-10 levels) and restructure the main UI to separate the Bible display area from the control area. It ensures the app preview matches the Presentation (PPT) output.

## User Review Required
> [!IMPORTANT]
> **Major UI Change**: The `DisplayPanel` (Main Screen) will be significantly refactored. The Bible text area will now reuse the `PresentationContent` component (scaled down) instead of a separate list. This ensures "What You See Is What You Get" (WYSIWYG) between the App and the PPT.

> [!NOTE]
> **Font Size Logic**: The requested font sizes (38sp - 74sp) match the existing logic in `PresentationContent`. We will expose this control to the user.

## Proposed Changes

### Logic & State
-   **Store**: `PresentationState` already has `fontSizeLevel`.
-   **ViewModel**: `PresentationViewModel` already has logic to update it.
-   **New**: Ensure `NavigationViewModel` or `MainScreen` can access/control this state easily if needed, or stick to `PresentationViewModel` as the source of truth for display settings.

### UI Refactoring
-   **DisplayPanel.kt**:
    -   Split into two distinct areas: `PreviewArea` (Top) and `ControlArea` (Bottom).
    -   **PreviewArea**: Use `PresentationContent` with a `scaleFactor` (e.g., calculated dynamically or fixed ~0.4) to render the Bible text exactly as it appears on the PPT.
    -   **ControlArea**:
        -   New `FontSizeControl`: A 1-10 slider/gauge.
        -   Existing `PageSizeSelector` (Verse Count).
        -   Existing Navigation Buttons (Prev, Next, PPT Mode).
    -   **Layout**: `FontSizeControl` placed *above* `PageSizeSelector`.

### Presentation Rendering
-   **Line Breaking**: Verify and refine `keepAll` logic to ensure words are not split mid-word unless necessary.
-   **Max Width**: Ensure the text container in the app preview mimics the max width constraint of the PPT window.

## Verification Plan

### Automated Tests
-   **Unit Test**: `PresentationViewModelTest` to verify `setFontSizeLevel` updates state correctly within 1-10 bounds.
-   **Unit Test**: `PresentationContentTest` (if exists) or new helper test to verify `getFontSizeForLevel` returns exact requested SP values (38, 42...74).

### Manual Verification
1.  **Visual Consistency**: Open App and PPT side-by-side.
    *   Change Font Level -> Both change synchronously.
    *   Verify Line Breaks -> Both should break at the same words (relative to their aspect ratio).
2.  **UI Layout**:
    *   Verify Controls are separated from the text.
    *   Verify "Font Size" slider is above "Verse Count".
3.  **Dual Monitor**:
    *   Connect 2nd monitor (or simulate).
    *   Toggle PPT.
    *   Verify control from main window updates PPT instantly.

---

## Phase 1: Logic Verification & Test Coverage (TDD)
**Goal**: Ensure the core logic for font size and state management is rock solid before touching UI.

- [x] **RED**:- [x] **Update `PresentationViewModelTest`**:
    -   `testInitialFontSizeLevel`: Verify default checks.
    -   `testSetFontSizeLevel`: Verify 1-10 boundary and state updates.
    -   `testPresentationContentScaling`: (Optional) If possible, verify scaling calc (might be UI test, so maybe skip or do unit test on helper function).
- [x] **Update `PresentationState`**: Add `fontSizeLevel: Int` (default 4).
- [x] **Run Tests**: Ensure all current tests pass.tionState` defaults to level 4. <!-- id: 2 -->

## Phase 2: UI Component & Layout Refactoring
**Goal**: Create the new UI structure (Split View) and the Font Control Component.

- [x] **Create Component**: `FontSizeControl` (Slider/Segmented Control). <!-- id: 3 -->
- [x] **Refactor `DisplayPanel`**: <!-- id: 4 -->
    -   Create `Column` layout.
    -   **Top**: `Box` for Preview. Replace `LazyColumn` of `VerseRow` with `PresentationContent(scaleFactor = ...)`.
    -   **Bottom**: `Card` or `Surface` for Control Area.
- [x] **Integrate**: Add `FontSizeControl` to the Control Area, positioned above `PageSizeSelector`. <!-- id: 5 -->

## Phase 3: Presentation Consistency & Refinement
**Goal**: Ensure the "Preview" really looks like the "PPT" and line breaking works.

- [x] **Scaling Logic**: Implement `scaleFactor` calculation in `PresentationContent` (or passed from parent) to fit the `PreviewArea` properly without scrolling (or with minimal scrolling if needed, but per request "PPT layout" implies fixed slide view). <!-- id: 6 -->
- [x] **Line Breaking**: Verify `keepAll` extension in `PresentationContent`. Ensure it applies to both App Preview and PPT. <!-- id: 7 -->
- [x] **Aspect Ratio**: Force `PreviewArea` to maintain target aspect ratio (e.g., 16:9) to strictly match PPT behavior. <!-- id: 8 -->

## Phase 4: Final Verification & Polish
**Goal**: Finalize visuals and strict user acceptance criteria.

- [x] **Strict Visual Check**: Compare "Provided Image description" (PPT Mode look) with App implementation. <!-- id: 9 -->
- [x] **Button Alignment**: Ensure Prev/Next/PPT buttons are aligned beautifully in the new Control Area. <!-- id: 10 -->
- [x] **Clean Code**: Remove old `VerseRow` if it is no longer used (or keep for other views if needed). <!-- id: 11 -->
