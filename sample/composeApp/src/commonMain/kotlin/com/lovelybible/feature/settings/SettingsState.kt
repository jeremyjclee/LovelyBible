package com.lovelybible.feature.settings

/**
 * 설정 상태
 */
data class SettingsState(
    val autoPptOnSearch: Boolean = false,
    val maxLineWidthBible: Int = 900, // 0~1920
    val maxLineWidthCreed: Int = 1120 // 0~1920
)
