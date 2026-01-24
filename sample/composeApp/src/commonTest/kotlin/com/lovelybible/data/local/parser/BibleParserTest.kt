package com.lovelybible.data.local.parser

import com.lovelybible.domain.model.Testament
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 2.2 - 성경 JSON 파서 테스트
 * - JSON 파싱 성공 확인
 * - 66권 책 목록 확인
 * - 특정 구절 조회 확인
 */
class BibleParserTest {
    
    private val parser = BibleJsonParser()
    
    /**
     * 간단한 JSON 파싱 테스트
     */
    @Test
    fun testJsonParsing_simpleFormat() {
        val json = """
            {
                "창1:1": "태초에 하나님이 천지를 창조하시니라",
                "창1:2": "땅이 혼돈하고 공허하며"
            }
        """.trimIndent()
        
        val verses = parser.parse(json)
        
        assertEquals(2, verses.size, "2개의 구절이 파싱되어야 함")
        assertEquals("창", verses[0].bookName)
        assertEquals(1, verses[0].chapter)
        assertEquals(1, verses[0].verse)
        assertEquals("태초에 하나님이 천지를 창조하시니라", verses[0].text)
    }
    
    /**
     * 다양한 책의 구절 파싱 테스트
     */
    @Test
    fun testJsonParsing_multipleBooks() {
        val json = """
            {
                "창1:1": "태초에 하나님이 천지를 창조하시니라",
                "출1:1": "야곱과 함께 각각 자기 가족을 데리고 애굽에 내려간",
                "마1:1": "아브라함과 다윗의 자손 예수 그리스도의 계보라"
            }
        """.trimIndent()
        
        val verses = parser.parse(json)
        
        assertEquals(3, verses.size, "3개의 구절이 파싱되어야 함")
        
        val bookNames = verses.map { it.bookName }.distinct()
        assertTrue(bookNames.contains("창"), "창세기가 포함되어야 함")
        assertTrue(bookNames.contains("출"), "출애굽기가 포함되어야 함")
        assertTrue(bookNames.contains("마"), "마태복음이 포함되어야 함")
    }
    
    /**
     * 책 목록 빌드 테스트 - 구약 책 확인
     */
    @Test
    fun testBuildBookList_oldTestament() {
        val json = """
            {
                "창1:1": "태초에 하나님이 천지를 창조하시니라",
                "창1:2": "땅이 혼돈하고 공허하며",
                "창2:1": "천지와 만물이 다 이루어지니라"
            }
        """.trimIndent()
        
        val verses = parser.parse(json)
        val books = parser.buildBookList(verses)
        
        assertEquals(1, books.size, "1개의 책이 있어야 함")
        assertEquals("창", books[0].name)
        assertEquals(Testament.OLD, books[0].testament, "창세기는 구약이어야 함")
        assertEquals(2, books[0].chapterCount, "창세기는 2개의 장이 파싱됨")
    }
    
    /**
     * 책 목록 빌드 테스트 - 신약 책 확인
     */
    @Test
    fun testBuildBookList_newTestament() {
        val json = """
            {
                "마1:1": "아브라함과 다윗의 자손 예수 그리스도의 계보라",
                "막1:1": "하나님의 아들 예수 그리스도의 복음의 시작이라"
            }
        """.trimIndent()
        
        val verses = parser.parse(json)
        val books = parser.buildBookList(verses)
        
        assertEquals(2, books.size, "2개의 책이 있어야 함")
        assertTrue(books.all { it.testament == Testament.NEW }, "모두 신약이어야 함")
    }
    
    /**
     * 특정 구절 조회 테스트
     */
    @Test
    fun testSpecificVerseRetrieval() {
        val json = """
            {
                "시23:1": "여호와는 나의 목자시니 내게 부족함이 없으리로다",
                "시23:2": "그가 나를 푸른 풀밭에 누이시며",
                "시23:3": "내 영혼을 소생시키시고"
            }
        """.trimIndent()
        
        val verses = parser.parse(json)
        val verse23_2 = verses.find { it.bookName == "시" && it.chapter == 23 && it.verse == 2 }
        
        assertNotNull(verse23_2, "시편 23:2를 찾을 수 있어야 함")
        assertEquals("그가 나를 푸른 풀밭에 누이시며", verse23_2.text)
    }
    
    /**
     * 빈 JSON 파싱 테스트
     */
    @Test
    fun testEmptyJsonParsing() {
        val json = "{}"
        
        val verses = parser.parse(json)
        
        assertTrue(verses.isEmpty(), "빈 JSON은 빈 리스트를 반환해야 함")
    }
    
    /**
     * 여러 장의 구절 파싱 테스트
     */
    @Test
    fun testMultipleChapters() {
        val json = """
            {
                "창1:1": "태초에",
                "창1:2": "땅이",
                "창2:1": "천지와",
                "창3:1": "뱀이"
            }
        """.trimIndent()
        
        val verses = parser.parse(json)
        val books = parser.buildBookList(verses)
        
        assertEquals(1, books.size)
        assertEquals(3, books[0].chapterCount, "창세기는 3개의 장을 가져야 함")
    }
    
    /**
     * 구약 39권 책 이름 테스트
     */
    @Test
    fun testOldTestamentBooks() {
        val oldTestamentBooks = listOf(
            "창", "출", "레", "민", "신", "수", "삿", "룻", "삼상", "삼하",
            "왕상", "왕하", "대상", "대하", "스", "느", "에", "욥", "시", "잠"
        )
        
        val json = oldTestamentBooks.mapIndexed { index, book ->
            "\"${book}1:1\": \"test$index\""
        }.joinToString(",", "{", "}")
        
        val verses = parser.parse(json)
        val books = parser.buildBookList(verses)
        
        assertTrue(books.all { it.testament == Testament.OLD }, "모두 구약이어야 함")
    }
    
    /**
     * 신약 27권 책 이름 테스트
     */
    @Test
    fun testNewTestamentBooks() {
        val newTestamentBooks = listOf(
            "마", "막", "눅", "요", "행", "롬", "고전", "고후", "갈", "엡",
            "빌", "골", "살전", "살후", "딤전", "딤후", "딛", "몬", "히", "약"
        )
        
        val json = newTestamentBooks.mapIndexed { index, book ->
            "\"${book}1:1\": \"test$index\""
        }.joinToString(",", "{", "}")
        
        val verses = parser.parse(json)
        val books = parser.buildBookList(verses)
        
        assertTrue(books.all { it.testament == Testament.NEW }, "모두 신약이어야 함")
    }
}
