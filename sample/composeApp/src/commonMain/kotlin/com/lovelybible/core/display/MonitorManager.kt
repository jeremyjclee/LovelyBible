package com.lovelybible.core.display

/**
 * 모니터 감지 및 관리 인터페이스
 */
interface MonitorManager {
    /**
     * 연결된 모든 디스플레이 조회
     */
    fun getAllDisplays(): List<DisplayInfo>
    
    /**
     * 외부 모니터 조회 (Primary가 아닌 첫 번째 모니터)
     */
    fun getExternalDisplay(): DisplayInfo?
    
    /**
     * 외부 모니터 존재 여부
     */
    fun hasExternalMonitor(): Boolean
    
    /**
     * 모니터 개수
     */
    fun getMonitorCount(): Int
}
