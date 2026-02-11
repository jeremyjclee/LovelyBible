package com.lovelybible.feature.presentation

/**
 * 프레젠테이션 Intent
 */
sealed class PresentationIntent {
    object TogglePresentation : PresentationIntent()  // 버튼용 (토글)
    object OpenPresentation : PresentationIntent()     // 단축키용 (ON만)
    object ClosePresentation : PresentationIntent()    // 단축키용 (OFF만)
    object RefreshMonitors : PresentationIntent()
    
    // 폰트 크기 조절
    data class SetFontSizeLevel(val level: Int) : PresentationIntent()
    
    // 내비게이션
    object NavigateNext : PresentationIntent()
    object NavigatePrevious : PresentationIntent()
}
