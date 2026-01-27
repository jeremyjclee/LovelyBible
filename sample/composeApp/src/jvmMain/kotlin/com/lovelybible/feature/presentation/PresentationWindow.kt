package com.lovelybible.feature.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.lovelybible.theme.LovelyBibleTheme

/**
 * 프레젠테이션 윈도우
 * 두 번째 모니터에 전체화면으로 표시
 */
@Composable
fun PresentationWindow(
    state: PresentationState,
    onClose: () -> Unit,
    onAction: (PresentationIntent) -> Unit,
    icon: Painter? = null
) {
    if (!state.isPresentationWindowOpen) return
    
    val displayInfo = state.displayInfo
    
    // 윈도우 위치 및 크기 계산
    val windowState = rememberWindowState(
        placement = WindowPlacement.Fullscreen,
        position = if (displayInfo != null) {
            WindowPosition(
                x = displayInfo.bounds.x.dp,
                y = displayInfo.bounds.y.dp
            )
        } else {
            WindowPosition.PlatformDefault
        },
        size = if (displayInfo != null) {
            DpSize(
                width = displayInfo.bounds.width.dp,
                height = displayInfo.bounds.height.dp
            )
        } else {
            DpSize(1920.dp, 1080.dp)
        }
    )
    
    Window(
        onCloseRequest = onClose,
        state = windowState,
        title = "Lovely Bible - Presentation",
        icon = icon,
        undecorated = true,  // 프레임 없음
        alwaysOnTop = true,
        onKeyEvent = { event ->
            handleKeyEvent(event, onClose, onAction)
        }
    ) {
        LovelyBibleTheme {
            PresentationContent(
                verses = state.currentVerses,
                title = state.currentTitle,
                fontSizeLevel = state.fontSizeLevel
            )
        }
    }
}

/**
 * 키 이벤트 처리
 */
private fun handleKeyEvent(
    event: KeyEvent, 
    onClose: () -> Unit,
    onAction: (PresentationIntent) -> Unit
): Boolean {
    if (event.type != androidx.compose.ui.input.key.KeyEventType.KeyDown) return false
    
    return when (event.key) {
        Key.Escape -> {
            onClose()
            true
        }
        Key.DirectionLeft, Key.DirectionUp -> {
            onAction(PresentationIntent.NavigatePrevious)
            true
        }
        Key.DirectionRight, Key.DirectionDown, Key.Spacebar -> {
            onAction(PresentationIntent.NavigateNext)
            true
        }
        else -> false
    }
}
