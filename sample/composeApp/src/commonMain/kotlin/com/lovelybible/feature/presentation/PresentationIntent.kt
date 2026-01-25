package com.lovelybible.feature.presentation

/**
 * 프레젠테이션 Intent
 */
sealed class PresentationIntent {
    object TogglePresentation : PresentationIntent()
    object ClosePresentation : PresentationIntent()
    object RefreshMonitors : PresentationIntent()
    
<<<<<<< HEAD
=======
    // 폰트 크기 조절
    data class SetFontSizeLevel(val level: Int) : PresentationIntent()
    
>>>>>>> d50ca95 (feat: font size control and main screen UI improvements)
    // 내비게이션
    object NavigateNext : PresentationIntent()
    object NavigatePrevious : PresentationIntent()
}
