package com.lovelybible.core.display

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 5.1 - 모니터 감지 서비스 테스트
 * - 연결된 디스플레이 수 반환
 * - 기본 디스플레이와 외부 디스플레이 구분
 * 
 * 참고: 실제 하드웨어 테스트는 JVM 환경에서만 가능하므로
 * 여기서는 인터페이스 계약을 테스트하는 Fake를 사용합니다.
 */
class MonitorManagerTest {
    
    /**
     * 단일 모니터 시나리오를 위한 Fake 구현
     */
    private class SingleMonitorManager : MonitorManager {
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
     * 듀얼 모니터 시나리오를 위한 Fake 구현
     */
    private class DualMonitorManager : MonitorManager {
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
     * 단일 모니터 - 디스플레이 수 반환 테스트
     */
    @Test
    fun testSingleMonitor_getMonitorCount() {
        val manager = SingleMonitorManager()
        
        assertEquals(1, manager.getMonitorCount(), "단일 모니터는 1을 반환해야 함")
    }
    
    /**
     * 단일 모니터 - 모든 디스플레이 조회 테스트
     */
    @Test
    fun testSingleMonitor_getAllDisplays() {
        val manager = SingleMonitorManager()
        
        val displays = manager.getAllDisplays()
        
        assertEquals(1, displays.size, "1개의 디스플레이가 있어야 함")
        assertTrue(displays[0].isPrimary, "유일한 디스플레이는 기본 디스플레이여야 함")
    }
    
    /**
     * 단일 모니터 - 외부 모니터 없음 테스트
     */
    @Test
    fun testSingleMonitor_hasExternalMonitor() {
        val manager = SingleMonitorManager()
        
        assertFalse(manager.hasExternalMonitor(), "단일 모니터는 외부 모니터가 없어야 함")
    }
    
    /**
     * 단일 모니터 - 외부 디스플레이 조회 (null) 테스트
     */
    @Test
    fun testSingleMonitor_getExternalDisplay() {
        val manager = SingleMonitorManager()
        
        assertNull(manager.getExternalDisplay(), "외부 디스플레이가 없으면 null이어야 함")
    }
    
    /**
     * 듀얼 모니터 - 디스플레이 수 반환 테스트
     */
    @Test
    fun testDualMonitor_getMonitorCount() {
        val manager = DualMonitorManager()
        
        assertEquals(2, manager.getMonitorCount(), "듀얼 모니터는 2를 반환해야 함")
    }
    
    /**
     * 듀얼 모니터 - 모든 디스플레이 조회 테스트
     */
    @Test
    fun testDualMonitor_getAllDisplays() {
        val manager = DualMonitorManager()
        
        val displays = manager.getAllDisplays()
        
        assertEquals(2, displays.size, "2개의 디스플레이가 있어야 함")
        assertEquals(1, displays.count { it.isPrimary }, "1개의 기본 디스플레이가 있어야 함")
        assertEquals(1, displays.count { !it.isPrimary }, "1개의 외부 디스플레이가 있어야 함")
    }
    
    /**
     * 듀얼 모니터 - 외부 모니터 있음 테스트
     */
    @Test
    fun testDualMonitor_hasExternalMonitor() {
        val manager = DualMonitorManager()
        
        assertTrue(manager.hasExternalMonitor(), "듀얼 모니터는 외부 모니터가 있어야 함")
    }
    
    /**
     * 듀얼 모니터 - 외부 디스플레이 조회 테스트
     */
    @Test
    fun testDualMonitor_getExternalDisplay() {
        val manager = DualMonitorManager()
        
        val external = manager.getExternalDisplay()
        
        assertNotNull(external, "외부 디스플레이가 있어야 함")
        assertFalse(external.isPrimary, "외부 디스플레이는 기본이 아니어야 함")
        assertEquals("External Monitor", external.name)
    }
    
    /**
     * 외부 디스플레이 위치 테스트 (듀얼 모니터)
     */
    @Test
    fun testDualMonitor_externalDisplayPosition() {
        val manager = DualMonitorManager()
        
        val external = manager.getExternalDisplay()
        
        assertNotNull(external)
        // 외부 모니터는 보통 기본 모니터 오른쪽에 위치
        assertTrue(external.bounds.x >= 0, "X 좌표는 0 이상이어야 함")
        assertEquals(2560, external.bounds.width, "외부 모니터 너비 확인")
        assertEquals(1440, external.bounds.height, "외부 모니터 높이 확인")
    }
    
    /**
     * 기본 디스플레이 확인 테스트
     */
    @Test
    fun testPrimaryDisplay() {
        val manager = DualMonitorManager()
        
        val primary = manager.getAllDisplays().find { it.isPrimary }
        
        assertNotNull(primary, "기본 디스플레이가 있어야 함")
        assertEquals(0, primary.bounds.x, "기본 디스플레이는 보통 (0,0)에서 시작")
        assertEquals(0, primary.bounds.y)
    }
    
    /**
     * DisplayInfo 데이터 클래스 속성 테스트
     */
    @Test
    fun testDisplayInfoProperties() {
        val bounds = DisplayBounds(
            x = 100,
            y = 200,
            width = 1920,
            height = 1080
        )
        val display = DisplayInfo(
            id = 0,
            name = "Test Display",
            bounds = bounds,
            isPrimary = true
        )
        
        assertEquals(0, display.id)
        assertEquals("Test Display", display.name)
        assertEquals(100, display.bounds.x)
        assertEquals(200, display.bounds.y)
        assertEquals(1920, display.bounds.width)
        assertEquals(1080, display.bounds.height)
        assertTrue(display.isPrimary)
    }
    
    /**
     * DisplayBounds 데이터 클래스 속성 테스트
     */
    @Test
    fun testDisplayBoundsProperties() {
        val bounds = DisplayBounds(
            x = 50,
            y = 100,
            width = 2560,
            height = 1440
        )
        
        assertEquals(50, bounds.x)
        assertEquals(100, bounds.y)
        assertEquals(2560, bounds.width)
        assertEquals(1440, bounds.height)
    }
}
