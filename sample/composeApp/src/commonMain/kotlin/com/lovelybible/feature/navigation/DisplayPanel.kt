package com.lovelybible.feature.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovelybible.domain.model.Verse
import com.lovelybible.theme.AppColors
import com.lovelybible.ui.components.GlassCard
import com.lovelybible.util.rememberBackgroundImage

/**
 * 디스플레이 패널 - 성경 구절 표시 및 내비게이션
 */
@Composable
fun DisplayPanel(
    state: NavigationState,
    onIntent: (NavigationIntent) -> Unit,
    presentationViewModel: com.lovelybible.feature.presentation.PresentationViewModel,
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    // 패널이 표시될 때 포커스 요청
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    GlassCard(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            if (state.canNavigatePrevious) {
                                onIntent(NavigationIntent.NavigatePrevious)
                            }
                            true
                        }
                        Key.DirectionRight -> {
                            if (state.canNavigateNext) {
                                onIntent(NavigationIntent.NavigateNext)
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 배경 이미지
            val backgroundImage = rememberBackgroundImage()
            if (backgroundImage != null) {
                Image(
                    bitmap = backgroundImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // 제목
                Text(
                    text = state.currentTitle.ifEmpty { "구절을 검색해주세요" },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2E7D32)  // 진한 초록색
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 구절 표시 영역
            if (state.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Accent)
                }
            } else if (state.error != null) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.Error
                    )
                }
            } else if (state.displayedVerses.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "검색 결과가 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.TextSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.displayedVerses.forEach { verse ->
                        val isCreed = verse.bookName == "사신"
                        VerseRow(verse = verse, hideVerseNumber = isCreed)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 페이지 크기 선택
            PageSizeSelector(
                currentSize = state.pageSize,
                onSizeChange = { onIntent(NavigationIntent.UpdatePageSize(it)) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 내비게이션 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이전 버튼
                NavigationArrowButton(
                    direction = ArrowDirection.LEFT,
                    enabled = state.canNavigatePrevious,
                    onClick = { onIntent(NavigationIntent.NavigatePrevious) }
                )
                
                // PPT 모드 버튼
                Button(
                    onClick = { 
                        presentationViewModel.onIntent(
                            com.lovelybible.feature.presentation.PresentationIntent.TogglePresentation
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (presentationViewModel.state.isPresentationWindowOpen) 
                            AppColors.Success else AppColors.Accent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PresentToAll,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (presentationViewModel.state.isPresentationWindowOpen) 
                            "PPT 종료" else "PPT 모드"
                    )
                }
                
                // 다음 버튼
                NavigationArrowButton(
                    direction = ArrowDirection.RIGHT,
                    enabled = state.canNavigateNext,
                    onClick = { onIntent(NavigationIntent.NavigateNext) }
                )
            }
            }
        }
    }
}

/**
 * 화살표 방향
 */
enum class ArrowDirection {
    LEFT, RIGHT
}

/**
 * 맛깔난 화살표 네비게이션 버튼
 */
@Composable
fun NavigationArrowButton(
    direction: ArrowDirection,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (enabled) {
        listOf(
            AppColors.Accent,
            AppColors.AccentHover
        )
    } else {
        listOf(
            AppColors.CardBackground,
            AppColors.CardBackground
        )
    }
    
    val icon = when (direction) {
        ArrowDirection.LEFT -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
        ArrowDirection.RIGHT -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    }
    
    val label = when (direction) {
        ArrowDirection.LEFT -> "이전"
        ArrowDirection.RIGHT -> "다음"
    }
    
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (enabled) AppColors.Accent.copy(alpha = 0.3f) else Color.Transparent,
                spotColor = if (enabled) AppColors.Accent.copy(alpha = 0.5f) else Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(gradientColors)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (direction == ArrowDirection.LEFT) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) Color.White else AppColors.TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) Color.White else AppColors.TextMuted,
                fontSize = 16.sp
            )
            
            if (direction == ArrowDirection.RIGHT) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) Color.White else AppColors.TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * 구절 행 컴포넌트
 */
@Composable
fun VerseRow(
    verse: Verse,
    hideVerseNumber: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        if (!hideVerseNumber) {
            Text(
                text = "${verse.verse}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2E7D32),  // 진한 초록색
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = verse.text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black  // 검정색
        )
    }
}

/**
 * 페이지 크기 선택기
 */
@Composable
fun PageSizeSelector(
    currentSize: Int,
    onSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "절 개수:",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )
        
        (1..10).forEach { size ->
            Text(
                text = "$size",
                style = MaterialTheme.typography.bodyMedium,
                color = if (size == currentSize) AppColors.Accent else AppColors.TextSecondary,
                modifier = Modifier
                    .clickable { onSizeChange(size) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
