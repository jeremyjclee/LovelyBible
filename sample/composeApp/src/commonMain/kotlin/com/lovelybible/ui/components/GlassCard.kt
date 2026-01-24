package com.lovelybible.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lovelybible.theme.AppColors

/**
 * Glassmorphism 스타일 카드 컴포넌트
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = AppColors.CardBackgroundAlpha,
        border = BorderStroke(1.dp, AppColors.BorderColor),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

/**
 * Glassmorphism 스타일 카드 (패딩 없음)
 */
@Composable
fun GlassCardNoPadding(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = AppColors.CardBackgroundAlpha,
        border = BorderStroke(1.dp, AppColors.BorderColor),
        shadowElevation = 8.dp
    ) {
        content()
    }
}
