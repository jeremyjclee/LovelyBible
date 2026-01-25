package com.lovelybible.feature.presentation

import com.lovelybible.core.display.DisplayBounds
import com.lovelybible.core.display.DisplayInfo
import com.lovelybible.core.display.MonitorManager
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse
import com.lovelybible.domain.repository.BibleRepository
import com.lovelybible.feature.navigation.NavigationViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 5 - PresentationViewModel 테스트
 * - 프레젠테이션 토글 동작
 * - 모니터 감지 통합
 * - NavigationViewModel 상태 동기화
 */
class PresentationViewModelTest {
    
    /**
     * 단일 모니터 Fake 구현
     */
    private class FakeSingleMonitorManager : MonitorManager {
        private val primaryDisplay = DisplayInfo(
            id = 0,
            name = "Primary Monitor",
            bounds = DisplayBounds(
                x = 0,
                y = 0,
                width = 1920,
                height = 1080
            ),
            isPrimary = true
        )
        
        override fun getAllDisplays(): List<DisplayInfo> = listOf(primaryDisplay)
        override fun getExternalDisplay(): DisplayInfo? = null
        override fun hasExternalMonitor(): Boolean = false
        override fun getMonitorCount(): Int = 1
    }
    
    /**
     * 듀얼 모니터 Fake 구현
     */
    private class FakeDualMonitorManager : MonitorManager {
        private val primaryDisplay = DisplayInfo(
            id = 0,
            name = "Primary Monitor",
            bounds = DisplayBounds(
                x = 0,
                y = 0,
                width = 1920,
                height = 1080
            ),
            isPrimary = true
        )
        
        private val externalDisplay = DisplayInfo(
            id = 1,
            name = "External Monitor",
            bounds = DisplayBounds(
                x = 1920,
                y = 0,
                width = 2560,
                height = 1440
            ),
            isPrimary = false
        )
        
        override fun getAllDisplays(): List<DisplayInfo> = listOf(primaryDisplay, externalDisplay)
        override fun getExternalDisplay(): DisplayInfo? = externalDisplay
        override fun hasExternalMonitor(): Boolean = true
        override fun getMonitorCount(): Int = 2
    }
    
    /**
     * Fake Repository for testing
     */
    private class FakeBibleRepository : BibleRepository {
        override suspend fun getAllBooks() = emptyList<Book>()
        override suspend fun getOldTestamentBooks() = emptyList<Book>()
        override suspend fun getNewTestamentBooks() = emptyList<Book>()
        override suspend fun getVerses(book: String, chapter: Int) = emptyList<Verse>()
        override suspend fun getVerse(book: String, chapter: Int, verse: Int): Verse? = null
        override suspend fun getChapterCount(book: String) = 0
        override suspend fun getVerseCount(book: String, chapter: Int) = 0
        override suspend fun searchBooks(query: String) = emptyList<Book>()
    }
    
    /**
     * 초기 상태 테스트
     */
    @Test
    fun testInitialState() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 초기 상태 확인
        assertFalse(viewModel.state.isActive, "초기 상태에서 프레젠테이션은 비활성화")
        assertFalse(viewModel.state.isPresentationWindowOpen, "초기 상태에서 창은 닫혀있음")
        assertEquals(PresentationMode.NONE, viewModel.state.mode, "초기 모드는 NONE")
    }
    
    /**
     * 단일 모니터에서 프레젠테이션 토글 테스트
     */
    @Test
    fun testTogglePresentation_singleMonitor() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 프레젠테이션 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        
        // 상태 확인
        assertTrue(viewModel.state.isPresentationWindowOpen, "창이 열려야 함")
        assertTrue(viewModel.state.isActive, "활성화 되어야 함")
        assertEquals(PresentationMode.LOCAL_OVERLAY, viewModel.state.mode, 
            "단일 모니터는 LOCAL_OVERLAY 모드여야 함")
    }
    
    /**
     * 듀얼 모니터에서 프레젠테이션 토글 테스트
     */
    @Test
    fun testTogglePresentation_dualMonitor() {
        val monitorManager = FakeDualMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 프레젠테이션 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        
        // 상태 확인
        assertTrue(viewModel.state.isPresentationWindowOpen, "창이 열려야 함")
        assertTrue(viewModel.state.hasExternalMonitor, "외부 모니터가 있어야 함")
        assertEquals(PresentationMode.EXTERNAL, viewModel.state.mode, 
            "듀얼 모니터는 EXTERNAL 모드여야 함")
        assertNotNull(viewModel.state.displayInfo, "디스플레이 정보가 있어야 함")
    }
    
    /**
     * 프레젠테이션 닫기 테스트
     */
    @Test
    fun testClosePresentation() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 먼저 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        
        // 닫기
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        
        // 상태 확인
        assertFalse(viewModel.state.isPresentationWindowOpen, "창이 닫혀야 함")
        assertFalse(viewModel.state.isActive, "비활성화 되어야 함")
        assertEquals(PresentationMode.NONE, viewModel.state.mode, "모드가 NONE이어야 함")
    }
    
    /**
     * 토글로 열고 닫기 테스트
     */
    @Test
    fun testTogglePresentation_openAndClose() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 첫 번째 토글: 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "첫 번째 토글: 열기")
        
        // 두 번째 토글: 닫기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen, "두 번째 토글: 닫기")
    }
    
    /**
     * 모니터 정보 갱신 테스트
     */
    @Test
    fun testRefreshMonitors() {
        val monitorManager = FakeDualMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 모니터 정보 갱신
        viewModel.onIntent(PresentationIntent.RefreshMonitors)
        
        // 상태 확인
        assertTrue(viewModel.state.hasExternalMonitor, "외부 모니터 정보가 갱신되어야 함")
        assertNotNull(viewModel.state.displayInfo, "디스플레이 정보가 있어야 함")
    }
    
    /**
     * Navigation 상태 업데이트 테스트
     */
    @Test
    fun testUpdateFromNavigation() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        val testVerses = listOf(
            Verse("창", 1, 1, "태초에 하나님이 천지를 창조하시니라")
        )
        val testTitle = "창세기 1장"
        
        // Navigation 상태 업데이트
        viewModel.updateFromNavigation(testVerses, testTitle)
        
        // 상태 확인
        assertEquals(testVerses, viewModel.state.currentVerses, "구절이 업데이트되어야 함")
        assertEquals(testTitle, viewModel.state.currentTitle, "제목이 업데이트되어야 함")
    }
    
    /**
     * 듀얼 모니터에서 외부 디스플레이 정보 확인 테스트
     */
    @Test
    fun testExternalDisplayInfo() {
        val monitorManager = FakeDualMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 프레젠테이션 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        
        val displayInfo = viewModel.state.displayInfo
        
        // 디스플레이 정보 확인
        assertNotNull(displayInfo, "외부 디스플레이 정보가 있어야 함")
        assertEquals(1, displayInfo.id, "외부 디스플레이 ID는 1")
        assertFalse(displayInfo.isPrimary, "외부 디스플레이는 기본이 아님")
        assertEquals(1920, displayInfo.bounds.x, "X 좌표는 기본 모니터 너비")
        assertEquals(2560, displayInfo.bounds.width, "외부 모니터 너비")
        assertEquals(1440, displayInfo.bounds.height, "외부 모니터 높이")
    }
<<<<<<< HEAD
=======
    
    /**
     * 초기 폰트 크기 레벨 테스트 (기본값 4)
     */
    @Test
    fun testInitialFontSizeLevel() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 초기 폰트 크기 레벨은 4
        assertEquals(4, viewModel.state.fontSizeLevel, "초기 폰트 크기 레벨은 4")
    }
    
    /**
     * 폰트 크기 레벨 설정 테스트
     */
    @Test
    fun testSetFontSizeLevel() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 레벨 1 설정
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(1))
        assertEquals(1, viewModel.state.fontSizeLevel, "레벨 1로 변경되어야 함")
        
        // 레벨 10 설정
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(10))
        assertEquals(10, viewModel.state.fontSizeLevel, "레벨 10으로 변경되어야 함")
        
        // 레벨 5 설정
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(5))
        assertEquals(5, viewModel.state.fontSizeLevel, "레벨 5로 변경되어야 함")
    }
    
    /**
     * 폰트 크기 레벨 범위 외 값 무시 테스트
     */
    @Test
    fun testSetFontSizeLevel_outOfRange() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel)
        
        // 초기값 확인
        assertEquals(4, viewModel.state.fontSizeLevel)
        
        // 범위 외 값 (0) - 무시되어야 함
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(0))
        assertEquals(4, viewModel.state.fontSizeLevel, "범위 외 값 0은 무시되어야 함")
        
        // 범위 외 값 (11) - 무시되어야 함
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(11))
        assertEquals(4, viewModel.state.fontSizeLevel, "범위 외 값 11은 무시되어야 함")
        
        // 범위 외 값 (-1) - 무시되어야 함
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(-1))
        assertEquals(4, viewModel.state.fontSizeLevel, "범위 외 값 -1은 무시되어야 함")
    }
>>>>>>> d50ca95 (feat: font size control and main screen UI improvements)
}
