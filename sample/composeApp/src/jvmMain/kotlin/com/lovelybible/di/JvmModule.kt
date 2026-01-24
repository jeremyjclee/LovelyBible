package com.lovelybible.di

import com.lovelybible.core.display.JvmMonitorManager
import com.lovelybible.core.display.MonitorManager
import com.lovelybible.core.resource.JvmResourceLoader
import com.lovelybible.core.resource.ResourceLoader
import org.koin.dsl.module

/**
 * JVM 플랫폼 전용 모듈
 */
val jvmModule = module {
    single<ResourceLoader> { JvmResourceLoader() }
    single<MonitorManager> { JvmMonitorManager() }
}
