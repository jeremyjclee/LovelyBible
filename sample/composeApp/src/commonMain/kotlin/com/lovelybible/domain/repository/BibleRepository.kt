package com.lovelybible.domain.repository

import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse

/**
 * 성경 데이터 Repository 인터페이스
 */
interface BibleRepository {
    
    /**
     * 모든 책 목록 조회
     */
    suspend fun getAllBooks(): List<Book>
    
    /**
     * 구약 책 목록 조회
     */
    suspend fun getOldTestamentBooks(): List<Book>
    
    /**
     * 신약 책 목록 조회
     */
    suspend fun getNewTestamentBooks(): List<Book>
    
    /**
     * 특정 책의 특정 장의 모든 구절 조회
     */
    suspend fun getVerses(book: String, chapter: Int): List<Verse>
    
    /**
     * 특정 구절 조회
     */
    suspend fun getVerse(book: String, chapter: Int, verse: Int): Verse?
    
    /**
     * 특정 책의 장 개수 조회
     */
    suspend fun getChapterCount(book: String): Int
    
    /**
     * 특정 책의 특정 장의 절 개수 조회
     */
    suspend fun getVerseCount(book: String, chapter: Int): Int
    
    /**
     * 책 이름으로 검색 (자동완성용)
     */
    suspend fun searchBooks(query: String): List<Book>
}
