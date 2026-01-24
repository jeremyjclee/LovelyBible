package com.lovelybible.feature.search

import com.lovelybible.domain.model.BiblePosition

/**
 * 검색 화면 Effect (일회성 이벤트)
 */
sealed class SearchEffect {
    data class NavigateToVerse(val position: BiblePosition) : SearchEffect()
    data class FocusField(val field: SearchField) : SearchEffect()
    data class ShowError(val message: String) : SearchEffect()
    object FocusDisplayPanel : SearchEffect()  // 성경 화면으로 포커스 이동
}

/**
 * 검색 필드 타입
 */
enum class SearchField {
    BOOK,
    CHAPTER,
    VERSE
}
