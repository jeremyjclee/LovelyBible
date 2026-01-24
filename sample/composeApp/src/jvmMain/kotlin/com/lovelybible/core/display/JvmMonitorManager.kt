package com.lovelybible.core.display

import java.awt.GraphicsEnvironment

/**
 * JVM용 모니터 매니저 구현
 */
class JvmMonitorManager : MonitorManager {
    private val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    
    override fun getAllDisplays(): List<DisplayInfo> {
        return ge.screenDevices.mapIndexed { index, device ->
            val bounds = device.defaultConfiguration.bounds
            DisplayInfo(
                id = index,
                name = device.iDstring,
                bounds = DisplayBounds(
                    x = bounds.x,
                    y = bounds.y,
                    width = bounds.width,
                    height = bounds.height
                ),
                isPrimary = device == ge.defaultScreenDevice
            )
        }
    }
    
    override fun getExternalDisplay(): DisplayInfo? {
        return getAllDisplays().find { !it.isPrimary }
    }
    
    override fun hasExternalMonitor(): Boolean {
        // screenDevices는 항상 메인 모니터를 포함하므로 2개 이상이어야 외부 모니터가 있다는 뜻
        return ge.screenDevices.size > 1
    }
    
    override fun getMonitorCount(): Int {
        return ge.screenDevices.size
    }
}
