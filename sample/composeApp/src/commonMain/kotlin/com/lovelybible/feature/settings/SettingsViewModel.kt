package com.lovelybible.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.lovelybible.domain.repository.SettingsRepository

/**
 * 설정 ViewModel (MVI 패턴)
 * 
 * tempState: 현재 편집 중인 임시 상태 (Dialog에서 수정)
 * savedState: 저장된 설정 (취소 시 복원용)
 */
class SettingsViewModel(
    private val repository: SettingsRepository
) {
    
    // 현재 편집 중인 임시 상태
    var state by mutableStateOf(SettingsState())
        private set
    
    // 저장된 상태 (취소 시 복원용)
    private var savedState = SettingsState()
    
    init {
        // 초기화: 저장소에서 설정 로드
        try {
            val isAutoPptOn = repository.isAutoPptOnSearch()
            val maxLinesBible = repository.getMaxLineWidthBible()
            val maxLinesCreed = repository.getMaxLineWidthCreed()
            val initialState = SettingsState(
                autoPptOnSearch = isAutoPptOn,
                maxLineWidthBible = maxLinesBible,
                maxLineWidthCreed = maxLinesCreed
            )
            state = initialState
            savedState = initialState.copy()
        } catch (e: Exception) {
            // 설정 로드 실패 시 기본값 사용
            val initialState = SettingsState()
            state = initialState
            savedState = initialState.copy()
        }
    }
    
    /**
     * Intent 처리
     */
    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateAutoPptOnSearch -> updateAutoPptOnSearch(intent.enabled)
            is SettingsIntent.UpdateMaxLineWidthBible -> updateMaxLineWidthBible(intent.width)
            is SettingsIntent.UpdateMaxLineWidthCreed -> updateMaxLineWidthCreed(intent.width)
            SettingsIntent.SaveSettings -> saveSettings()
            SettingsIntent.CancelSettings -> cancelSettings()
        }
    }
    
    /**
     * 자동 PPT 설정 변경 (tempState만 수정)
     */
    private fun updateAutoPptOnSearch(enabled: Boolean) {
        state = state.copy(autoPptOnSearch = enabled)
    }

    /**
     * 일반 성경 최대 줄 너비 변경 (tempState만 수정)
     */
    private fun updateMaxLineWidthBible(width: Int) {
        val safeWidth = width.coerceIn(0, 1920)
        state = state.copy(maxLineWidthBible = safeWidth)
    }

    /**
     * 사도 신경 최대 줄 너비 변경 (tempState만 수정)
     */
    private fun updateMaxLineWidthCreed(width: Int) {
        val safeWidth = width.coerceIn(0, 1920)
        state = state.copy(maxLineWidthCreed = safeWidth)
    }
    
    /**
     * 설정 저장 (tempState → savedState 커밋 & Repository 저장)
     */
    private fun saveSettings() {
        savedState = state.copy()
        // Repository에 저장
        try {
            repository.setAutoPptOnSearch(state.autoPptOnSearch)
            repository.setMaxLineWidthBible(state.maxLineWidthBible)
            repository.setMaxLineWidthCreed(state.maxLineWidthCreed)
        } catch (e: Exception) {
            // 저장 실패 시 무시 (다음 실행 시 기본값 사용)
        }
    }
    
    /**
     * 설정 취소 (savedState → tempState 복원)
     */
    private fun cancelSettings() {
        state = savedState.copy()
    }
}
