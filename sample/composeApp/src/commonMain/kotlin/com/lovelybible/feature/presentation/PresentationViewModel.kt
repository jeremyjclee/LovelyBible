package com.lovelybible.feature.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lovelybible.core.display.MonitorManager
import com.lovelybible.feature.navigation.NavigationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 프레젠테이션 ViewModel
 * NavigationViewModel의 상태를 구독하여 프레젠테이션 창에 동기화
 */
class PresentationViewModel(
    private val monitorManager: MonitorManager,
    private val navigationViewModel: NavigationViewModel
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // State
    var state by mutableStateOf(PresentationState())
        private set
    
    init {
        // NavigationViewModel 상태 구독
        scope.launch {
            navigationViewModel.state.let { navState ->
                // 초기 상태 동기화
                state = state.copy(
                    currentVerses = navState.displayedVerses,
                    currentTitle = navState.currentTitle
                )
            }
        }
        
        // 모니터 정보 초기화
        refreshMonitors()
    }
    
    /**
     * Intent 처리
     */
    fun onIntent(intent: PresentationIntent) {
        when (intent) {
            PresentationIntent.TogglePresentation -> togglePresentation()
            PresentationIntent.ClosePresentation -> closePresentation()
            PresentationIntent.RefreshMonitors -> refreshMonitors()
            
            // 내비게이션 위임
            PresentationIntent.NavigateNext -> navigationViewModel.onIntent(com.lovelybible.feature.navigation.NavigationIntent.NavigateNext)
            PresentationIntent.NavigatePrevious -> navigationViewModel.onIntent(com.lovelybible.feature.navigation.NavigationIntent.NavigatePrevious)
        }
    }
    
    /**
     * NavigationViewModel 상태 업데이트 시 호출
     */
    fun updateFromNavigation(verses: List<com.lovelybible.domain.model.Verse>, title: String) {
        state = state.copy(
            currentVerses = verses,
            currentTitle = title
        )
    }
    
    /**
     * 프레젠테이션 토글
     */
    fun togglePresentation() {
        if (state.isPresentationWindowOpen) {
            closePresentation()
        } else {
            openPresentation()
        }
    }
    
    /**
     * 프레젠테이션 열기
     */
    private fun openPresentation() {
        val hasExternal = monitorManager.hasExternalMonitor()
        val externalDisplay = monitorManager.getExternalDisplay()
        
        state = state.copy(
            isPresentationWindowOpen = true,
            hasExternalMonitor = hasExternal,
            displayInfo = externalDisplay,
            mode = if (hasExternal) PresentationMode.EXTERNAL else PresentationMode.LOCAL_OVERLAY,
            isActive = true
        )
    }
    
    /**
     * 프레젠테이션 닫기
     */
    private fun closePresentation() {
        state = state.copy(
            isPresentationWindowOpen = false,
            mode = PresentationMode.NONE,
            isActive = false
        )
    }
    
    /**
     * 모니터 정보 갱신
     */
    private fun refreshMonitors() {
        val hasExternal = monitorManager.hasExternalMonitor()
        val externalDisplay = monitorManager.getExternalDisplay()
        
        state = state.copy(
            hasExternalMonitor = hasExternal,
            displayInfo = externalDisplay
        )
    }
}
