package com.lovelybible.feature.navigation

import com.lovelybible.domain.model.BiblePosition
import com.lovelybible.domain.model.Verse

/**
 * 내비게이션 State (현재 표시 상태)
 */
data class NavigationState(
    val currentPosition: BiblePosition? = null,
    val displayedVerses: List<Verse> = emptyList(),
    val currentTitle: String = "",
    val pageSize: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val canNavigatePrevious: Boolean = false,
    val canNavigateNext: Boolean = false
)
