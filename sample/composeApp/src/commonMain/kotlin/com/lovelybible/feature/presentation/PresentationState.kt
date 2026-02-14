package com.lovelybible.feature.presentation

import com.lovelybible.core.display.DisplayInfo
import com.lovelybible.domain.model.Verse

/**
 * 프레젠테이션 모드
 */
enum class PresentationMode {
    NONE,           // 비활성
    EXTERNAL,       // 외부 모니터 전체화면
    LOCAL_OVERLAY   // 로컬 전체화면 오버레이
}

/**
 * 프레젠테이션 State
 */
data class PresentationState(
    val isActive: Boolean = false,
    val isPresentationWindowOpen: Boolean = false,
    val hasExternalMonitor: Boolean = false,
    val displayInfo: DisplayInfo? = null,
    val currentVerses: List<Verse> = emptyList(),
    val currentTitle: String = "",
    val mode: PresentationMode = PresentationMode.NONE,
    val fontSizeLevel: Int = 2,  // 1~10단계, 기본값 2 (40.sp)
    val maxLineWidth: Int = 1120
)
