package com.lovelybible.feature.search

import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.BiblePosition

/**
 * 검색 화면 Intent (사용자 액션)
 */
sealed class SearchIntent {
    data class UpdateBookQuery(val query: String) : SearchIntent()
    data class UpdateChapter(val chapter: String) : SearchIntent()
    data class UpdateVerse(val verse: String) : SearchIntent()
    data class SelectBook(val book: Book, val autoSearch: Boolean = false) : SearchIntent()
    object ExecuteSearch : SearchIntent()
    object ClearSearch : SearchIntent()
    object ClearBookSelection : SearchIntent()  // 책 선택만 해제 (수정 모드 진입)
    data class SelectRecentSearch(val position: BiblePosition) : SearchIntent()
    object ClearRecentSearches : SearchIntent()  // 최근 검색 초기화
}
