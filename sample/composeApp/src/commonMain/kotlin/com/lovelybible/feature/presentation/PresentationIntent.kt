package com.lovelybible.feature.presentation

/**
 * 프레젠테이션 Intent
 */
sealed class PresentationIntent {
    object TogglePresentation : PresentationIntent()
    object ClosePresentation : PresentationIntent()
    object RefreshMonitors : PresentationIntent()
    
    // 내비게이션
    object NavigateNext : PresentationIntent()
    object NavigatePrevious : PresentationIntent()
}
