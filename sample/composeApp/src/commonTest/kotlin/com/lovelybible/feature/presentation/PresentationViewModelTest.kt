package com.lovelybible.feature.presentation

import com.lovelybible.core.display.DisplayBounds
import com.lovelybible.core.display.DisplayInfo
import com.lovelybible.core.display.MonitorManager
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse
import com.lovelybible.domain.repository.BibleRepository
import com.lovelybible.feature.navigation.NavigationViewModel
import com.lovelybible.test.fake.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PresentationViewModelTest {
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 모니터 정보 갱신
        viewModel.onIntent(PresentationIntent.RefreshMonitors)
        
        // 상태 확인
        assertTrue(viewModel.state.hasExternalMonitor, "외부 모니터 정보가 갱신되어야 함")
        assertNotNull(viewModel.state.displayInfo, "디스플레이 정보가 있어야 함")
    }
    
    /**
     * Navigation 상태 업데이트 테스트
     */
    /**
     * Navigation 상태 업데이트 테스트
     */
    @Test
    fun testUpdateFromNavigation() = runTest {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        advanceUntilIdle() // Let NavigationViewModel init finish
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
    
    /**
     * 초기 폰트 크기 레벨 테스트 (기본값 4)
     */
    @Test
    fun testInitialFontSizeLevel() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 초기 폰트 크기 레벨은 4 -> 2로 변경됨. PLAN에서 2로 변경하기로 함. PresentationState 기본값 확인 필요.
        // 하지만 State 기본값은 2로 변경했음. 테스트도 업데이트.
        assertEquals(2, viewModel.state.fontSizeLevel, "초기 폰트 크기 레벨은 2")
    }
    
    /**
     * 폰트 크기 레벨 설정 테스트
     */
    @Test
    fun testSetFontSizeLevel() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
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
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 초기값 확인 (2)
        assertEquals(2, viewModel.state.fontSizeLevel)
        
        // 범위 외 값 (0) - 무시되어야 함
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(0))
        assertEquals(2, viewModel.state.fontSizeLevel, "범위 외 값 0은 무시되어야 함")
        
        // 범위 외 값 (11) - 무시되어야 함
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(11))
        assertEquals(2, viewModel.state.fontSizeLevel, "범위 외 값 11은 무시되어야 함")
        
        // 범위 외 값 (-1) - 무시되어야 함
        viewModel.onIntent(PresentationIntent.SetFontSizeLevel(-1))
        assertEquals(2, viewModel.state.fontSizeLevel, "범위 외 값 -1은 무시되어야 함")
    }
    
    // ============================================================
    // Phase 1: PPT 모드 보호 테스트
    // ============================================================
    
    /**
     * ClosePresentation intent만이 PPT를 닫을 수 있음
     */
    @Test
    fun testOnlyExplicitCloseCanClosePPT() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // PPT 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "PPT가 열려야 함")
        assertTrue(viewModel.state.isActive, "활성화 되어야 함")
        
        // 명시적 ClosePresentation으로만 닫힘
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen, "ClosePresentation으로 닫혀야 함")
        assertFalse(viewModel.state.isActive, "비활성화 되어야 함")
        assertEquals(PresentationMode.NONE, viewModel.state.mode, "모드가 NONE이어야 함")
    }
    
    /**
     * TogglePresentation으로 열고 ClosePresentation으로 닫기 — 상태 정확성
     */
    @Test
    fun testClosePresentation_afterToggle_stateIsCorrect() {
        val monitorManager = FakeDualMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // Toggle로 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        assertEquals(PresentationMode.EXTERNAL, viewModel.state.mode)
        
        // Close로 닫기
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen, "닫혀야 함")
        assertFalse(viewModel.state.isActive, "비활성화 되어야 함")
        assertEquals(PresentationMode.NONE, viewModel.state.mode, "NONE이어야 함")
    }
    
    /**
     * 연속 토글 사이클에서 상태 일관성 유지
     */
    @Test
    fun testMultipleToggleCycles_stateRemainsConsistent() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 5번 반복 토글
        repeat(5) { i ->
            // 열기
            viewModel.onIntent(PresentationIntent.TogglePresentation)
            assertTrue(viewModel.state.isPresentationWindowOpen, "사이클 ${i+1}: 열려야 함")
            assertTrue(viewModel.state.isActive, "사이클 ${i+1}: 활성화 되어야 함")
            assertEquals(PresentationMode.LOCAL_OVERLAY, viewModel.state.mode, "사이클 ${i+1}: LOCAL_OVERLAY")
            
            // 닫기
            viewModel.onIntent(PresentationIntent.TogglePresentation)
            assertFalse(viewModel.state.isPresentationWindowOpen, "사이클 ${i+1}: 닫혀야 함")
            assertFalse(viewModel.state.isActive, "사이클 ${i+1}: 비활성화 되어야 함")
            assertEquals(PresentationMode.NONE, viewModel.state.mode, "사이클 ${i+1}: NONE")
        }
    }
    
    // ============================================================
    // Phase 2: Spacebar 토글 테스트
    // ============================================================
    
    /**
     * Spacebar: PPT OFF → ON (TogglePresentation으로 시뮬레이션)
     */
    @Test
    fun testSpacebar_whenPPTOff_turnsOn() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        assertFalse(viewModel.state.isPresentationWindowOpen, "처음에는 꺼져있어야 함")
        
        // Spacebar = TogglePresentation
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "Spacebar로 켜져야 함")
        assertTrue(viewModel.state.isActive, "활성화 되어야 함")
    }
    
    /**
     * Spacebar: PPT ON → OFF (TogglePresentation으로 시뮬레이션)
     */
    @Test
    fun testSpacebar_whenPPTOn_turnsOff() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 먼저 켜기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        
        // Spacebar = TogglePresentation으로 끄기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen, "Spacebar로 꺼져야 함")
        assertFalse(viewModel.state.isActive, "비활성화 되어야 함")
        assertEquals(PresentationMode.NONE, viewModel.state.mode)
    }
    
    /**
     * 연속 Spacebar 토글
     */
    @Test
    fun testSpacebar_multipleToggles() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // OFF → ON
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        
        // ON → OFF
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen)
        
        // OFF → ON
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        
        // ON → OFF
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen)
    }
    
    // ============================================================
    // Phase 3: PPT ON/OFF 상태 동기화 테스트
    // ============================================================
    
    /**
     * ClosePresentation 후 상태 완전 초기화 확인
     */
    @Test
    fun testClosePresentation_stateFullyReset() {
        val monitorManager = FakeDualMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        assertTrue(viewModel.state.isActive)
        assertEquals(PresentationMode.EXTERNAL, viewModel.state.mode)
        
        // 닫기
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        
        // 모든 상태 필드 초기화 확인
        assertFalse(viewModel.state.isPresentationWindowOpen, "isPresentationWindowOpen은 false")
        assertFalse(viewModel.state.isActive, "isActive는 false")
        assertEquals(PresentationMode.NONE, viewModel.state.mode, "mode는 NONE")
    }
    
    /**
     * Toggle → Close → Toggle 사이클에서 상태 정확성
     */
    @Test
    fun testToggleCloseToggle_stateSync() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // Toggle로 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "1차 열기")
        
        // Close로 닫기
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen, "Close로 닫기")
        
        // 다시 Toggle로 열기 — 1번의 토글로 바로 열려야 함 (2번 누를 필요 없음)
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "1번의 토글로 다시 열려야 함")
        assertTrue(viewModel.state.isActive, "활성화 되어야 함")
    }
    
    /**
     * isPresentationWindowOpen, isActive, mode 모두 동기화 확인
     */
    @Test
    fun testAllStateFieldsSynced_afterClose() {
        val monitorManager = FakeDualMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 열기
        viewModel.onIntent(PresentationIntent.TogglePresentation)
        
        // 세 필드 모두 "열려있음" 상태
        assertTrue(viewModel.state.isPresentationWindowOpen)
        assertTrue(viewModel.state.isActive)
        assertEquals(PresentationMode.EXTERNAL, viewModel.state.mode)
        
        // 닫기
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        
        // 세 필드 모두 "닫혀있음" 상태 — 하나라도 다르면 버튼 동기화 실패
        assertFalse(viewModel.state.isPresentationWindowOpen, "isPresentationWindowOpen 동기화 실패")
        assertFalse(viewModel.state.isActive, "isActive 동기화 실패")
        assertEquals(PresentationMode.NONE, viewModel.state.mode, "mode 동기화 실패")
    }
    
    // ============================================================
    // OpenPresentation (ON만) 테스트
    // ============================================================
    
    /**
     * OpenPresentation: OFF → ON
     */
    @Test
    fun testOpenPresentation_whenOff_turnsOn() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        assertFalse(viewModel.state.isPresentationWindowOpen)
        
        viewModel.onIntent(PresentationIntent.OpenPresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "ON이 되어야 함")
        assertTrue(viewModel.state.isActive, "활성화 되어야 함")
    }
    
    /**
     * OpenPresentation: 이미 ON이면 그대로 ON 유지 (토글 안 됨)
     */
    @Test
    fun testOpenPresentation_whenAlreadyOn_staysOn() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // 먼저 ON
        viewModel.onIntent(PresentationIntent.OpenPresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        
        // 다시 Open → 여전히 ON (OFF로 전환되면 안 됨)
        viewModel.onIntent(PresentationIntent.OpenPresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "이미 ON이면 ON 유지")
        assertTrue(viewModel.state.isActive, "활성화 유지")
    }
    
    /**
     * Open → Close → Open 사이클
     */
    @Test
    fun testOpenCloseOpen_cycle() {
        val monitorManager = FakeSingleMonitorManager()
        val navigationViewModel = NavigationViewModel(FakeBibleRepository())
        val settingsRepository = FakeSettingsRepository()
        val viewModel = PresentationViewModel(monitorManager, navigationViewModel, settingsRepository)
        
        // Open
        viewModel.onIntent(PresentationIntent.OpenPresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen)
        
        // Close
        viewModel.onIntent(PresentationIntent.ClosePresentation)
        assertFalse(viewModel.state.isPresentationWindowOpen)
        
        // Open again
        viewModel.onIntent(PresentationIntent.OpenPresentation)
        assertTrue(viewModel.state.isPresentationWindowOpen, "다시 열려야 함")
    }
}
