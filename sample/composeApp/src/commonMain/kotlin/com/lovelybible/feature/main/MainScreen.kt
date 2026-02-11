package com.lovelybible.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.lovelybible.feature.navigation.DisplayPanel
import com.lovelybible.feature.navigation.NavigationViewModel
import com.lovelybible.feature.presentation.PresentationViewModel
import com.lovelybible.feature.search.SearchEffect
import com.lovelybible.feature.search.SearchPanel
import com.lovelybible.feature.search.SearchViewModel
import com.lovelybible.theme.AppColors
import org.koin.compose.koinInject

/**
 * LovelyBible 메인 화면
 * 40:60 비율의 2단 레이아웃
 */
@Composable
fun MainScreen() {
    val searchViewModel: SearchViewModel = koinInject()
    val navigationViewModel: NavigationViewModel = koinInject()
    val presentationViewModel: PresentationViewModel = koinInject()
    val settingsViewModel: com.lovelybible.feature.settings.SettingsViewModel = koinInject()
    
    // 책 검색 필드 포커스 관리 (검색 단축키용)
    val bookFocusRequester = remember { FocusRequester() }

    // DisplayPanel 포커스 관리
    val displayFocusRequester = remember { FocusRequester() }
    
    // 전역 포커스를 위한 FocusRequester
    val rootFocusRequester = remember { FocusRequester() }
    
    // FocusDisplayPanel effect 처리
    LaunchedEffect(Unit) {
        searchViewModel.effect.collect { effect ->
            if (effect is SearchEffect.FocusDisplayPanel) {
                displayFocusRequester.requestFocus()
            }
        }
    }
    
    // 앱 시작 시 루트에 포커스 설정
    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(20.dp)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    // Ctrl + F: 책 검색 필드로 포커스 이동
                    if (event.isCtrlPressed && event.key == Key.F) {
                        bookFocusRequester.requestFocus()
                        return@onPreviewKeyEvent true
                    }
                    
                    when (event.key) {
                        // F5 / Spacebar: PPT 모드 ON만 (이미 ON이면 무시)
                        Key.Spacebar, Key.F5 -> {
                            if (!presentationViewModel.state.isPresentationWindowOpen) {
                                presentationViewModel.onIntent(
                                    com.lovelybible.feature.presentation.PresentationIntent.OpenPresentation
                                )
                            }
                            true
                        }
                        // Esc: PPT 모드 OFF만 (이미 OFF이면 무시)
                        Key.Escape -> {
                            if (presentationViewModel.state.isPresentationWindowOpen) {
                                presentationViewModel.onIntent(
                                    com.lovelybible.feature.presentation.PresentationIntent.ClosePresentation
                                )
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 왼쪽: 선택/검색 패널 (40%)
        SelectionPanel(
            searchViewModel = searchViewModel,
            bookFocusRequester = bookFocusRequester,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        )
        
        // 오른쪽: 디스플레이 패널 (60%)
        DisplayPanel(
            state = navigationViewModel.state,
            onIntent = navigationViewModel::onIntent,
            presentationViewModel = presentationViewModel,
            settingsViewModel = settingsViewModel,
            focusRequester = displayFocusRequester,
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        )
    }
}

/**
 * 왼쪽 선택 패널 - 책 선택, 검색, 설정 등
 */
@Composable
fun SelectionPanel(
    searchViewModel: SearchViewModel,
    bookFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 책 선택 그리드 - 메인 콘텐츠 (58%)
        BookListPanel(
            searchViewModel = searchViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
        )
        
        // 검색 패널 - 하단 독(Dock) 형태 (42%)
        SearchPanel(
            state = searchViewModel.state,
            onIntent = searchViewModel::onIntent,
            effectFlow = searchViewModel.effect,
            bookFocusRequester = bookFocusRequester,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
        )
    }
}
