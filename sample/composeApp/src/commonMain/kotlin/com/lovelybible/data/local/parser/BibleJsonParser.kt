package com.lovelybible.data.local.parser

import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * 성경 JSON 파서
 * 형식: {"창1:1": "텍스트", "창1:2": "텍스트", ...}
 */
class BibleJsonParser {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * JSON 문자열을 파싱하여 Verse 리스트로 변환
     */
    fun parse(jsonString: String): List<Verse> {
        val verses = mutableListOf<Verse>()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        jsonObject.forEach { (key, value) ->
            parseVerseKey(key)?.let { (bookName, chapter, verse) ->
                val text = value.toString().trim('"')
                verses.add(Verse(bookName, chapter, verse, text))
            }
        }
        
        return verses
    }
    
    /**
     * "창1:1" 형식의 키를 파싱
     * @return Triple(책이름, 장, 절) 또는 null
     */
    private fun parseVerseKey(key: String): Triple<String, Int, Int>? {
        // 정규식: 책이름 + 숫자 + : + 숫자
        val regex = """^(.+?)(\d+):(\d+)$""".toRegex()
        val match = regex.find(key) ?: return null
        
        val (bookName, chapterStr, verseStr) = match.destructured
        val chapter = chapterStr.toIntOrNull() ?: return null
        val verse = verseStr.toIntOrNull() ?: return null
        
        return Triple(bookName, chapter, verse)
    }
    
    /**
     * Verse 리스트로부터 Book 리스트 생성
     */
    fun buildBookList(verses: List<Verse>): List<Book> {
        val bookMap = mutableMapOf<String, MutableSet<Int>>()
        
        verses.forEach { verse ->
            bookMap.getOrPut(verse.bookName) { mutableSetOf() }.add(verse.chapter)
        }
        
        return bookMap.entries.mapIndexed { index, (bookName, chapters) ->
            Book(
                id = index + 1,
                name = bookName,
                testament = determineTestament(bookName),
                chapterCount = chapters.size
            )
        }.sortedBy { it.id }
    }
    
    /**
     * 책 이름으로 구약/신약 판별
     */
    private fun determineTestament(bookName: String): Testament {
        return if (bookName in OLD_TESTAMENT_BOOKS) {
            Testament.OLD
        } else {
            Testament.NEW
        }
    }
    
    companion object {
        // 구약 책 목록 (39권)
        private val OLD_TESTAMENT_BOOKS = setOf(
            "창", "출", "레", "민", "신",
            "수", "삿", "룻", "삼상", "삼하",
            "왕상", "왕하", "대상", "대하", "스",
            "느", "에", "욥", "시", "잠",
            "전", "아", "사", "렘", "애",
            "겔", "단", "호", "욜", "암",
            "옵", "욘", "미", "나", "합",
            "습", "학", "슥", "말"
        )
    }
}
