package com.lovelybible.feature.settings

/**
 * 설정 상태
 */
data class SettingsState(
    val autoPptOnSearch: Boolean = false,
    val maxLineWidth: Int = 1120 // 0~1920
)
