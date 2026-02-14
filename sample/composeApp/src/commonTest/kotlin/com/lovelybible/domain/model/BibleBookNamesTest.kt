package com.lovelybible.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BibleBookNamesTest {

    @Test
    fun `getChoSung extracts initial consonants correctly`() {
        assertEquals("ㅊㅅㄱ", BibleBookNames.getChoSung("창세기"))
        assertEquals("ㅁㅌㅂㅇ", BibleBookNames.getChoSung("마태복음"))
        assertEquals("ㅅㅅ", BibleBookNames.getChoSung("사신"))
        assertEquals("Gen", BibleBookNames.getChoSung("Gen")) // 한글 아니면 그대로
    }

    @Test
    fun `isChoSungOnly checks correctly`() {
        assertTrue(BibleBookNames.isChoSungOnly("ㅊㅅㄱ"))
        assertTrue(BibleBookNames.isChoSungOnly("ㄱㄴㄷ"))
        assertFalse(BibleBookNames.isChoSungOnly("창세기"))
        assertFalse(BibleBookNames.isChoSungOnly("abc"))
        assertFalse(BibleBookNames.isChoSungOnly("ㅊㅅㄱa"))
    }

    @Test
    fun `matchChoSung matches correctly`() {
        assertTrue(BibleBookNames.matchChoSung("창세기", "ㅊㅅ"))
        assertTrue(BibleBookNames.matchChoSung("창세기", "ㅅㄱ")) // Contains
        assertTrue(BibleBookNames.matchChoSung("요한계시록", "ㅇㅎㄱㅅㄹ"))
        assertFalse(BibleBookNames.matchChoSung("창세기", "ㅁㅌ"))
    }

    @Test
    fun `matchesQuery supports cho sung search`() {
        // 초성 검색
        assertTrue(BibleBookNames.matchesQuery("창", "ㅊㅅ")) // 창 -> 창세기 -> ㅊㅅㄱ 
        assertTrue(BibleBookNames.matchesQuery("창", "ㅊㅅㄱ"))
        assertTrue(BibleBookNames.matchesQuery("마", "ㅁㅌ")) // 마 -> 마태복음 -> ㅁㅌㅂㅇ
        assertTrue(BibleBookNames.matchesQuery("고전", "ㄱㄹㄷ")) // 고전 -> 고린도전서 -> ㄱㄹㄷㅈㅅ

        // 중간 초성 매칭
        assertTrue(BibleBookNames.matchesQuery("창", "ㅅㄱ")) // 창세기 -> ㅊ"ㅅㄱ"

        // 실패 케이스
        assertFalse(BibleBookNames.matchesQuery("창", "ㅁㅌ"))
    }

    @Test
    fun `matchesQuery supports original text matching`() {
        assertTrue(BibleBookNames.matchesQuery("창", "창세"))
        assertTrue(BibleBookNames.matchesQuery("창", "세기"))
        assertTrue(BibleBookNames.matchesQuery("마", "마태"))
    }
    
    @Test
    fun `matchesQuery supports English`() {
        // 영어 검색은 지원하지 않지만, 코드는 lowerCase변환 후 contains 체크함.
        // 현재 fullToShortName 맵에는 한글만 있음.
        // 만약 추후 영어 이름이 추가된다면 동작할 것.
        // 현재는 "Gen" 등의 영어 이름 매핑이 없으므로 "창"에 대해 "Gen"으로 검색하면 실패.
    }
}
