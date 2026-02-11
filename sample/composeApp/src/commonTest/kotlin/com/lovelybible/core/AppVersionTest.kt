package com.lovelybible.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppVersionTest {
    
    @Test
    fun testAppVersion_isCorrect() {
        assertEquals("1.0.5", APP_VERSION)
    }
    
    @Test
    fun testAppVersion_isNotEmpty() {
        assertTrue(APP_VERSION.isNotEmpty(), "앱 버전은 비어있지 않아야 함")
    }
}
