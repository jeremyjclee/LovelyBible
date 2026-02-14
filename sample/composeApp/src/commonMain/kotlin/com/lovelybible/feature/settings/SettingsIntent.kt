package com.lovelybible.feature.settings

/**
 * 설정 Intent
 */
sealed class SettingsIntent {
    /**
     * 검색 시 자동 PPT ON 설정 변경
     */
    data class UpdateAutoPptOnSearch(val enabled: Boolean) : SettingsIntent()
    
    /**
     * 최대 줄 너비 설정 변경 (0~1920)
     */
    data class UpdateMaxLineWidth(val width: Int) : SettingsIntent()
    
    /**
     * 설정 저장 (tempState → savedState)
     */
    object SaveSettings : SettingsIntent()
    
    /**
     * 설정 취소 (savedState → tempState 복원)
     */
    object CancelSettings : SettingsIntent()
}
