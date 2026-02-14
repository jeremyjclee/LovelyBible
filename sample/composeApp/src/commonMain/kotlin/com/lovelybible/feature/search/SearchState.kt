package com.lovelybible.feature.search

import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.BiblePosition

/**
 * 검색 화면 State (UI 상태)
 */
data class SearchState(
    val bookQuery: String = "",
    val selectedBook: Book? = null,
    val chapterInput: String = "",
    val verseInput: String = "",
    val suggestions: List<Book> = emptyList(),
    val recentSearches: List<BiblePosition> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val focusedSuggestionIndex: Int = -1, // 키보드 네비게이션용 포커스 인덱스 (-1: 없음)
    val isChoSungMatch: Boolean = false // 초성 검색 결과인지 여부 (자동 선택 방지용)
) {
    val isValid: Boolean
        get() = selectedBook != null && 
                chapterInput.toIntOrNull() != null && 
                verseInput.toIntOrNull() != null
}
