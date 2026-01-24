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
    val error: String? = null
) {
    val isValid: Boolean
        get() = selectedBook != null && 
                chapterInput.toIntOrNull() != null && 
                verseInput.toIntOrNull() != null
}
