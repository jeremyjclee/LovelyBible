package com.lovelybible.di

import com.lovelybible.data.local.parser.BibleJsonParser
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 1.3 - Koin DI 모듈 테스트
 * - AppModule 내 모든 의존성 정의 확인
 * - 의존성 그래프 검증
 */
class AppModuleTest : KoinTest {
    
    @BeforeTest
    fun setup() {
        stopKoin()
    }
    
    @AfterTest
    fun tearDown() {
        stopKoin()
    }
    
    /**
     * AppModule이 예외 없이 로드되는지 확인
     */
    @Test
    fun testAppModuleLoads() {
        val koinApp = startKoin {
            modules(appModule)
        }
        
        assertNotNull(koinApp, "Koin App이 null이 아니어야 함")
        assertTrue(koinApp.koin.getAll<BibleJsonParser>().isNotEmpty(), 
            "BibleJsonParser가 등록되어 있어야 함")
    }
    
    /**
     * BibleJsonParser가 싱글톤으로 등록되어 있는지 확인
     */
    @Test
    fun testBibleJsonParserIsSingleton() {
        val koinApp = startKoin {
            modules(appModule)
        }
        
        val parser1 = koinApp.koin.get<BibleJsonParser>()
        val parser2 = koinApp.koin.get<BibleJsonParser>()
        
        assertTrue(parser1 === parser2, "BibleJsonParser는 싱글톤이어야 함")
    }
    
    /**
     * 모든 모듈 리스트가 올바르게 정의되어 있는지 확인
     */
    @Test
    fun testAllModulesListIsNotEmpty() {
        assertTrue(allModules.isNotEmpty(), "allModules 리스트가 비어있지 않아야 함")
        assertTrue(allModules.contains(appModule), "allModules에 appModule이 포함되어야 함")
    }
}
