package com.lovelybible.data.repository

import com.lovelybible.data.local.parser.BibleJsonParser
import com.lovelybible.domain.model.Testament
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 2.4 - 성경 Repository 테스트
 * - 책 목록 조회
 * - 구절 조회
 * - 검색 기능
 */
class BibleRepositoryTest {
    
    private val testJson = """
        {
            "창1:1": "태초에 하나님이 천지를 창조하시니라",
            "창1:2": "땅이 혼돈하고 공허하며",
            "창1:3": "하나님이 이르시되 빛이 있으라 하시니",
            "창2:1": "천지와 만물이 다 이루어지니라",
            "출1:1": "야곱과 함께",
            "마1:1": "아브라함과 다윗의 자손"
        }
    """.trimIndent()
    
    private fun createRepository(): BibleRepositoryImpl {
        return BibleRepositoryImpl(
            parser = BibleJsonParser(),
            jsonLoader = { testJson }
        )
    }
    
    /**
     * 모든 책 목록 조회 테스트
     */
    @Test
    fun testGetAllBooks() = runTest {
        val repository = createRepository()
        
        val books = repository.getAllBooks()
        
        assertEquals(3, books.size, "3개의 책이 있어야 함")
        assertTrue(books.any { it.name == "창" }, "창세기가 있어야 함")
        assertTrue(books.any { it.name == "출" }, "출애굽기가 있어야 함")
        assertTrue(books.any { it.name == "마" }, "마태복음이 있어야 함")
    }
    
    /**
     * 구약 책 목록 조회 테스트
     */
    @Test
    fun testGetOldTestamentBooks() = runTest {
        val repository = createRepository()
        
        val oldBooks = repository.getOldTestamentBooks()
        
        assertEquals(2, oldBooks.size, "구약 2개의 책이 있어야 함")
        assertTrue(oldBooks.all { it.testament == Testament.OLD }, "모두 구약이어야 함")
    }
    
    /**
     * 신약 책 목록 조회 테스트
     */
    @Test
    fun testGetNewTestamentBooks() = runTest {
        val repository = createRepository()
        
        val newBooks = repository.getNewTestamentBooks()
        
        assertEquals(1, newBooks.size, "신약 1개의 책이 있어야 함")
        assertTrue(newBooks.all { it.testament == Testament.NEW }, "모두 신약이어야 함")
    }
    
    /**
     * 특정 장의 구절 조회 테스트
     */
    @Test
    fun testGetVerses() = runTest {
        val repository = createRepository()
        
        val verses = repository.getVerses("창", 1)
        
        assertEquals(3, verses.size, "창세기 1장에 3개의 구절이 있어야 함")
        assertTrue(verses.all { it.chapter == 1 }, "모든 절이 1장이어야 함")
    }
    
    /**
     * 특정 구절 조회 테스트
     */
    @Test
    fun testGetVerse() = runTest {
        val repository = createRepository()
        
        val verse = repository.getVerse("창", 1, 1)
        
        assertNotNull(verse, "창세기 1:1을 찾을 수 있어야 함")
        assertEquals("창", verse.bookName)
        assertEquals(1, verse.chapter)
        assertEquals(1, verse.verse)
        assertEquals("태초에 하나님이 천지를 창조하시니라", verse.text)
    }
    
    /**
     * 존재하지 않는 구절 조회 테스트
     */
    @Test
    fun testGetVerse_notFound() = runTest {
        val repository = createRepository()
        
        val verse = repository.getVerse("창", 100, 100)
        
        assertNull(verse, "존재하지 않는 구절은 null이어야 함")
    }
    
    /**
     * 장 개수 조회 테스트
     */
    @Test
    fun testGetChapterCount() = runTest {
        val repository = createRepository()
        
        val chapterCount = repository.getChapterCount("창")
        
        assertEquals(2, chapterCount, "창세기는 2개의 장이 있어야 함")
    }
    
    /**
     * 절 개수 조회 테스트
     */
    @Test
    fun testGetVerseCount() = runTest {
        val repository = createRepository()
        
        val verseCount = repository.getVerseCount("창", 1)
        
        assertEquals(3, verseCount, "창세기 1장에 3개의 절이 있어야 함")
    }
    
    /**
     * 책 검색 테스트 - 정확한 이름
     */
    @Test
    fun testSearchBooks_exactName() = runTest {
        val repository = createRepository()
        
        val results = repository.searchBooks("창")
        
        assertEquals(1, results.size, "1개의 결과가 있어야 함")
        assertEquals("창", results[0].name)
    }
    
    /**
     * 책 검색 테스트 - 부분 이름
     */
    @Test
    fun testSearchBooks_partialName() = runTest {
        val repository = createRepository()
        
        // 대소문자 무관 검색
        val results = repository.searchBooks("마")
        
        assertEquals(1, results.size)
        assertEquals("마", results[0].name)
    }
    
    /**
     * 책 검색 테스트 - 빈 쿼리
     */
    @Test
    fun testSearchBooks_emptyQuery() = runTest {
        val repository = createRepository()
        
        val results = repository.searchBooks("")
        
        assertTrue(results.isEmpty(), "빈 쿼리는 빈 결과를 반환해야 함")
    }
    
    /**
     * 책 검색 테스트 - 결과 없음
     */
    @Test
    fun testSearchBooks_noResults() = runTest {
        val repository = createRepository()
        
        val results = repository.searchBooks("존재하지않는책")
        
        assertTrue(results.isEmpty(), "존재하지 않는 책은 빈 결과를 반환해야 함")
    }
    
    /**
     * 캐싱 테스트 - 동일한 데이터 반복 조회
     */
    @Test
    fun testCaching() = runTest {
        val repository = createRepository()
        
        // 첫 번째 조회
        val books1 = repository.getAllBooks()
        // 두 번째 조회 (캐시에서)
        val books2 = repository.getAllBooks()
        
        assertEquals(books1.size, books2.size, "캐시된 결과는 동일해야 함")
    }
    
    /**
     * 존재하지 않는 책의 구절 조회 테스트
     */
    @Test
    fun testGetVerses_nonExistentBook() = runTest {
        val repository = createRepository()
        
        val verses = repository.getVerses("없는책", 1)
        
        assertTrue(verses.isEmpty(), "존재하지 않는 책은 빈 리스트를 반환해야 함")
    }
}
