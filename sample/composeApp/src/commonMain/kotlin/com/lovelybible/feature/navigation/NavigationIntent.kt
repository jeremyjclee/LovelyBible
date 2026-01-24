package com.lovelybible.feature.navigation

import com.lovelybible.domain.model.BiblePosition
import com.lovelybible.domain.model.Verse

/**
 * 내비게이션 Intent (사용자 액션)
 */
sealed class NavigationIntent {
    data class NavigateToPosition(val position: BiblePosition) : NavigationIntent()
    object NavigatePrevious : NavigationIntent()
    object NavigateNext : NavigationIntent()
    data class UpdatePageSize(val size: Int) : NavigationIntent()
}
