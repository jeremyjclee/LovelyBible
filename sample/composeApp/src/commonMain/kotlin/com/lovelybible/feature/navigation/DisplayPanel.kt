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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovelybible.domain.model.Verse
import com.lovelybible.feature.presentation.PresentationContent
import com.lovelybible.feature.presentation.PresentationIntent
import com.lovelybible.theme.AppColors
import com.lovelybible.ui.components.FontSizeControl
import com.lovelybible.ui.components.GlassCard
import com.lovelybible.util.rememberBackgroundImage

/**
 * 디스플레이 패널 - 성경 구절 표시 및 내비게이션
 * Preview Area (PPT 미리보기) + Control Area (조작부)
 */
@Composable
fun DisplayPanel(
    state: NavigationState,
    onIntent: (NavigationIntent) -> Unit,
    presentationViewModel: com.lovelybible.feature.presentation.PresentationViewModel,
    settingsViewModel: com.lovelybible.feature.settings.SettingsViewModel,
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    // 패널이 표시될 때 포커스 요청
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // 설정 다이얼로그 표시 상태
    var showSettingsDialog by remember { mutableStateOf(false) }
    
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
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Preview Area (상단, 가변 높이)
            // PPT 화면과 동일한 비율/구성을 보여줌
            // 상단 설정 버튼 (Overlap 방지)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = { showSettingsDialog = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "설정",
                        tint = AppColors.TextSecondary
                    )
                }
            }

            // 1. Preview Area (상단, 가변 높이)
            // PPT 화면과 동일한 비율/구성을 보여줌
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .aspectRatio(16f / 9f)
                        .fillMaxWidth() // 가로 꽉 채우기 시도 (aspectRatio로 높이 결정됨)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color.White) // PPT 기본 배경색
                ) {
                    // 스케일 계산: 기준 너비 1920dp 대비 현재 너비 비율
                    // aspectRatio(16/9)가 적용되었으므로, maxWidth는 1920 비율에 맞게 조정됨
                    val targetWidth = 1920f
                    val scale = (maxWidth.value / targetWidth).coerceAtLeast(0.2f)
                    
                    if (state.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AppColors.Accent)
                        }
                    } else if (state.error != null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.error, color = AppColors.Error)
                        }
                    } else {
                        // PPT 콘텐츠 재사용
                        PresentationContent(
                            verses = state.displayedVerses,
                            title = state.currentTitle,
                            fontSizeLevel = presentationViewModel.state.fontSizeLevel,
                            scaleFactor = scale,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = AppColors.BorderColor)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 2. Control Area (하단, 조작부)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 글자 크기 조절 (게이지)
                FontSizeControl(
                    currentLevel = presentationViewModel.state.fontSizeLevel,
                    onLevelChange = { level ->
                        presentationViewModel.onIntent(PresentationIntent.SetFontSizeLevel(level))
                    }
                )
                
                // 절 개수 선택
                PageSizeSelector(
                    currentSize = state.pageSize,
                    onSizeChange = { onIntent(NavigationIntent.UpdatePageSize(it)) }
                )
                
                // 내비게이션 버튼 (이전 - PPT - 다음)
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
                                PresentationIntent.TogglePresentation
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
    
    // 설정 다이얼로그
    if (showSettingsDialog) {
        com.lovelybible.feature.settings.SettingsDialog(
            state = settingsViewModel.state,
            onUpdateAutoPpt = { enabled ->
                settingsViewModel.onIntent(
                    com.lovelybible.feature.settings.SettingsIntent.UpdateAutoPptOnSearch(enabled)
                )
            },
            onSave = {
                settingsViewModel.onIntent(com.lovelybible.feature.settings.SettingsIntent.SaveSettings)
            },
            onCancel = {
                settingsViewModel.onIntent(com.lovelybible.feature.settings.SettingsIntent.CancelSettings)
            },
            onDismiss = { showSettingsDialog = false }
        )
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
        
        // 1..10 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..10).forEach { size ->
                val isSelected = size == currentSize
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) AppColors.Accent else Color.Transparent
                        )
                        .clickable { onSizeChange(size) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$size",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color.White else AppColors.TextSecondary,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else null
                    )
                }
            }
        }
    }
}

