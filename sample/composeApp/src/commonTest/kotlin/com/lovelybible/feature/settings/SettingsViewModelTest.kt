package com.lovelybible.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * SettingsViewModel 테스트
 */
import com.lovelybible.test.fake.FakeSettingsRepository

/**
 * SettingsViewModel 테스트
 */
class SettingsViewModelTest {
    
    /**
     * 초기 상태 확인: Repository 값을 반영해야 함
     */
    @Test
    fun testInitialState_reflectsRepository() {
        val repository = FakeSettingsRepository()
        repository.setAutoPptOnSearch(true) // 기본값 설정
        
        val viewModel = SettingsViewModel(repository)
        
        assertTrue(viewModel.state.autoPptOnSearch, "Repository의 값(true)을 반영해야 함")
    }
    
    /**
     * 자동 PPT 설정 변경
     */
    @Test
    fun testUpdateAutoPptOnSearch_updatesState() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        
        // OFF → ON
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(true))
        assertTrue(viewModel.state.autoPptOnSearch, "ON으로 변경되어야 함")
        
        // ON → OFF
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(false))
        assertFalse(viewModel.state.autoPptOnSearch, "OFF로 변경되어야 함")
    }
    
    /**
     * 설정 저장: tempState → savedState 커밋 & Repository 저장
     */
    @Test
    fun testSaveSettings_commitsToRepository() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        
        // 1. 설정 변경
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(true))
        
        // 2. 저장
        viewModel.onIntent(SettingsIntent.SaveSettings)
        
        // 3. Repository 업데이트 확인
        assertTrue(repository.isAutoPptOnSearch(), "Repository에 true가 저장되어야 함")
        
        // 4. 취소해도 상태 유지 (이미 저장됨)
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(false))
        viewModel.onIntent(SettingsIntent.CancelSettings)
        assertTrue(viewModel.state.autoPptOnSearch, "저장된 상태(true)로 복원되어야 함")
    }
    
    /**
     * 설정 취소: savedState → tempState 복원
     */
    @Test
    fun testCancelSettings_revertsToSavedState() {
        val repository = FakeSettingsRepository()
        repository.setAutoPptOnSearch(false) // 초기값 false
        val viewModel = SettingsViewModel(repository)
        
        // 1. 설정 변경 (저장 안 함)
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(true))
        assertTrue(viewModel.state.autoPptOnSearch)
        
        // 2. 취소 → 초기 상태(false)로 복원
        viewModel.onIntent(SettingsIntent.CancelSettings)
        assertFalse(viewModel.state.autoPptOnSearch, "초기 상태(false)로 복원되어야 함")
        assertFalse(repository.isAutoPptOnSearch(), "Repository는 변경되지 않아야 함")
    }
    
    /**
     * 저장 → 수정 → 취소 사이클
     */
    @Test
    fun testSaveModifyCancel_cycle() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        
        // Cycle 1: OFF → ON → 저장
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(true))
        viewModel.onIntent(SettingsIntent.SaveSettings)
        assertTrue(viewModel.state.autoPptOnSearch, "Cycle 1: ON 저장")
        assertTrue(repository.isAutoPptOnSearch(), "Repo: ON 저장")
        
        // Cycle 2: ON → OFF → 취소 → ON 복원
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(false))
        assertFalse(viewModel.state.autoPptOnSearch, "Cycle 2: OFF로 변경")
        viewModel.onIntent(SettingsIntent.CancelSettings)
        assertTrue(viewModel.state.autoPptOnSearch, "Cycle 2: 취소 후 ON 복원")
        
        // Cycle 3: ON → OFF → 저장
        viewModel.onIntent(SettingsIntent.UpdateAutoPptOnSearch(false))
        viewModel.onIntent(SettingsIntent.SaveSettings)
        assertFalse(viewModel.state.autoPptOnSearch, "Cycle 3: OFF 저장")
        assertFalse(repository.isAutoPptOnSearch(), "Repo: OFF 저장")
    }
}
