package com.lovelybible.data.repository

import com.lovelybible.data.local.parser.BibleJsonParser
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse
import com.lovelybible.domain.repository.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 성경 데이터 Repository 구현
 * In-Memory 캐싱 전략 사용
 */
class BibleRepositoryImpl(
    private val parser: BibleJsonParser,
    private val jsonLoader: () -> String
) : BibleRepository {
    
    // In-Memory Cache
    private var cachedVerses: Map<String, Map<Int, List<Verse>>>? = null
    private var cachedBooks: List<Book>? = null
    
    /**
     * 데이터 로딩 보장
     */
    private suspend fun ensureLoaded() {
        if (cachedVerses == null) {
            withContext(Dispatchers.Default) {
                val json = jsonLoader()
                val verses = parser.parse(json)
                
                // 책별 -> 장별 -> 구절 리스트로 그룹화
                cachedVerses = verses.groupBy { it.bookName }
                    .mapValues { (_, bookVerses) ->
                        bookVerses.groupBy { it.chapter }
                    }
                
                cachedBooks = parser.buildBookList(verses)
            }
        }
    }
    
    override suspend fun getAllBooks(): List<Book> {
        ensureLoaded()
        return cachedBooks ?: emptyList()
    }
    
    override suspend fun getOldTestamentBooks(): List<Book> {
        ensureLoaded()
        return cachedBooks?.filter { it.testament == Testament.OLD } ?: emptyList()
    }
    
    override suspend fun getNewTestamentBooks(): List<Book> {
        ensureLoaded()
        return cachedBooks?.filter { it.testament == Testament.NEW } ?: emptyList()
    }
    
    override suspend fun getVerses(book: String, chapter: Int): List<Verse> {
        ensureLoaded()
        return cachedVerses?.get(book)?.get(chapter) ?: emptyList()
    }
    
    override suspend fun getVerse(book: String, chapter: Int, verse: Int): Verse? {
        ensureLoaded()
        return cachedVerses?.get(book)?.get(chapter)?.find { it.verse == verse }
    }
    
    override suspend fun getChapterCount(book: String): Int {
        ensureLoaded()
        return cachedVerses?.get(book)?.keys?.size ?: 0
    }
    
    override suspend fun getVerseCount(book: String, chapter: Int): Int {
        ensureLoaded()
        return cachedVerses?.get(book)?.get(chapter)?.size ?: 0
    }
    
    override suspend fun searchBooks(query: String): List<Book> {
        ensureLoaded()
        if (query.isBlank()) return emptyList()
        
        return cachedBooks?.filter { book ->
            com.lovelybible.domain.model.BibleBookNames.matchesQuery(book.name, query)
        } ?: emptyList()
    }
}
