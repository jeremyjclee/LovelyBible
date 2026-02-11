package com.lovelybible.feature.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovelybible.domain.model.BibleBookNames
import com.lovelybible.domain.model.Book
import com.lovelybible.theme.AppColors
import com.lovelybible.ui.components.GlassCard

/**
 * 검색 패널 UI
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchPanel(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    effectFlow: kotlinx.coroutines.flow.Flow<SearchEffect>,
    bookFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    // FocusRequester 정의
    val chapterFocusRequester = remember { FocusRequester() }
    val verseFocusRequester = remember { FocusRequester() }
    
    // Backspace 입력 감지를 위한 상태
    var isDeleting by remember { mutableStateOf(false) }
    // 이전 쿼리 길이 추적
    var previousQueryLength by remember { mutableStateOf(0) }
    
    // 선택된 책의 풀네임
    val selectedBookFullName = state.selectedBook?.let { 
        BibleBookNames.toFullName(it.name) 
    }
    
    // Effect 처리 - FocusField로 포커스 이동
    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is SearchEffect.FocusField -> {
                    when (effect.field) {
                        SearchField.BOOK -> bookFocusRequester.requestFocus()
                        SearchField.CHAPTER -> chapterFocusRequester.requestFocus()
                        SearchField.VERSE -> verseFocusRequester.requestFocus()
                    }
                }
                else -> { /* 다른 effect는 여기서 처리하지 않음 */ }
            }
        }
    }
    
    // 자동 선택: 후보가 1개일 때 자동으로 선택하고 장 필드로 이동
    // 단, Backspace로 지우는 중이 아닐 때만 적용
    LaunchedEffect(state.suggestions, isDeleting) {
        if (state.suggestions.size == 1 && 
            state.selectedBook == null && 
            state.bookQuery.isNotBlank() &&
            !isDeleting) {
            onIntent(SearchIntent.SelectBook(state.suggestions.first(), autoSearch = false))
            chapterFocusRequester.requestFocus()
        }
    }
    
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 헤더 - 작고 파란색
            Text(
                text = "검색",
                style = MaterialTheme.typography.titleSmall,
                color = AppColors.Accent,
                fontWeight = FontWeight.Bold
            )
            
            // 책/장/절 입력 - 한 줄로 배치
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 책 검색 필드 (40%)
                BookSearchField(
                    query = state.bookQuery,
                    suggestions = state.suggestions,
                    isBookSelected = state.selectedBook != null,
                    focusRequester = bookFocusRequester,
                    onQueryChange = { newQuery ->
                        // 쿼리가 변경되면 선택된 책 초기화 (수정 모드로 전환)
                        val currentLength = state.bookQuery.length
                        isDeleting = newQuery.length < currentLength
                        previousQueryLength = newQuery.length
                        
                        // 선택된 책이 있고 쿼리가 변경되면 선택 해제
                        if (state.selectedBook != null) {
                            onIntent(SearchIntent.ClearBookSelection)
                        }
                        onIntent(SearchIntent.UpdateBookQuery(newQuery))
                    },
                    onSelectBook = { book ->
                        isDeleting = false
                        onIntent(SearchIntent.SelectBook(book, autoSearch = false))
                    },
                    onEnterOrTabPressed = {
                        // 첫 번째 제안이 있으면 선택, 없으면 그냥 다음으로 이동
                        if (state.suggestions.isNotEmpty()) {
                            isDeleting = false
                            onIntent(SearchIntent.SelectBook(state.suggestions.first(), autoSearch = false))
                        } else {
                            // 제안이 없을 때만 수동으로 포커스 이동 (제안 선택 시에는 VM에서 Effect 발생)
                            chapterFocusRequester.requestFocus()
                        }
                    },
                    modifier = Modifier.weight(0.4f)
                )
                
                // 장 입력 (30%)
                OutlinedTextField(
                    value = state.chapterInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onIntent(SearchIntent.UpdateChapter(newValue))
                        }
                    },
                    label = { Text("장", color = AppColors.TextSecondary, fontSize = 11.sp) },
                    placeholder = { Text("1", color = AppColors.TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .weight(0.3f)
                        .focusRequester(chapterFocusRequester)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && 
                                (event.key == Key.Enter || event.key == Key.Tab)) {
                                // 장 입력 상태에서 엔터/탭: 절이 입력되어 있으면 검색, 아니면 절 필드로 이동
                                if (state.verseInput.isNotEmpty()) {
                                    onIntent(SearchIntent.ExecuteSearch)
                                    // 검색 후 책 이름 필드로 포커스 이동 (선택사항, UX에 따라 조정)
                                    bookFocusRequester.requestFocus()
                                } else {
                                    verseFocusRequester.requestFocus()
                                }
                                true
                            } else {
                                false
                            }
                        },
                    textStyle = TextStyle(
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { 
                            if (state.verseInput.isNotEmpty()) {
                                onIntent(SearchIntent.ExecuteSearch)
                                bookFocusRequester.requestFocus()
                            } else {
                                verseFocusRequester.requestFocus() 
                            }
                        }
                    ),
                    singleLine = true,
                    colors = searchTextFieldColors()
                )
                
                // 절 입력 (30%)
                OutlinedTextField(
                    value = state.verseInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onIntent(SearchIntent.UpdateVerse(newValue))
                        }
                    },
                    label = { Text("절", color = AppColors.TextSecondary, fontSize = 11.sp) },
                    placeholder = { Text("1", color = AppColors.TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .weight(0.3f)
                        .focusRequester(verseFocusRequester)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && 
                                (event.key == Key.Enter || event.key == Key.Tab)) {
                                // 절 입력 상태에서 엔터/탭: 절이 입력되어 있으면 검색, 아니면 현 위치 유지
                                if (state.verseInput.isNotEmpty()) {
                                    onIntent(SearchIntent.ExecuteSearch)
                                    // 검색 후 책 이름 필드로 포커스 이동
                                    bookFocusRequester.requestFocus()
                                } else {
                                    // 절이 비어있으면 아무 동작 안함 (또는 포커스 유지)
                                    verseFocusRequester.requestFocus()
                                }
                                true
                            } else {
                                false
                            }
                        },
                    textStyle = TextStyle(
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { 
                            if (state.verseInput.isNotEmpty()) {
                                onIntent(SearchIntent.ExecuteSearch)
                                bookFocusRequester.requestFocus()
                            }
                        }
                    ),
                    singleLine = true,
                    colors = searchTextFieldColors()
                )
            }
            
            // 최근 검색
            if (state.recentSearches.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 최근 검색 헤더 (타이틀 + 초기화 버튼)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "최근 검색",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.TextSecondary
                        )
                        TextButton(
                            onClick = { onIntent(SearchIntent.ClearRecentSearches) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("초기화", color = AppColors.TextSecondary, fontSize = 10.sp)
                        }
                    }
                    
                    // 5개 이하: 1줄, 6개 이상: 2줄 (최근 5개 윗줄, 나머지 아랫줄)
                    val recentCount = state.recentSearches.size
                    val topRowItems = state.recentSearches.take(5)
                    val bottomRowItems = if (recentCount > 5) state.recentSearches.drop(5) else emptyList()
                    
                    // 윗줄 (최근 5개)
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(topRowItems.size) { index ->
                            val position = topRowItems[index]
                            val fullName = BibleBookNames.toFullName(position.book)
                            val label = "$fullName ${position.chapter}:${position.verse}"
                            
                            Surface(
                                onClick = { onIntent(SearchIntent.SelectRecentSearch(position)) },
                                shape = RoundedCornerShape(16.dp),
                                color = AppColors.CardBackground,
                                border = BorderStroke(1.dp, AppColors.Accent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = AppColors.TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    // 아랫줄 (6개 이상일 때만 표시)
                    if (bottomRowItems.isNotEmpty()) {
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(bottomRowItems.size) { index ->
                                val position = bottomRowItems[index]
                                val fullName = BibleBookNames.toFullName(position.book)
                                val label = "$fullName ${position.chapter}:${position.verse}"
                                
                                Surface(
                                    onClick = { onIntent(SearchIntent.SelectRecentSearch(position)) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppColors.CardBackground,
                                    border = BorderStroke(1.dp, AppColors.Accent.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = AppColors.TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 검색 필드 공통 색상
 */
@Composable
private fun searchTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.TextPrimary,
    unfocusedTextColor = AppColors.TextPrimary,
    disabledTextColor = AppColors.TextSecondary,
    cursorColor = AppColors.Accent,
    focusedBorderColor = AppColors.Accent,
    unfocusedBorderColor = AppColors.BorderColor,
    disabledBorderColor = AppColors.BorderColor.copy(alpha = 0.5f),
    focusedLabelColor = AppColors.Accent,
    unfocusedLabelColor = AppColors.TextSecondary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent
)

/**
 * 책 검색 필드 (자동완성 포함)
 */
@Composable
fun BookSearchField(
    query: String,
    suggestions: List<Book>,
    isBookSelected: Boolean,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSelectBook: (Book) -> Unit,
    onEnterOrTabPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { 
                Text(
                    if (isBookSelected) "책 이름 (선택됨 - 수정하려면 타이핑하세요)" else "책 이름", 
                    color = AppColors.TextSecondary,
                    fontSize = if (isBookSelected) 10.sp else 12.sp
                ) 
            },
            placeholder = { 
                Text(
                    "예: 창세기, 요한복음", 
                    color = AppColors.TextSecondary.copy(alpha = 0.5f)
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && 
                        (event.key == Key.Enter || event.key == Key.Tab)) {
                        onEnterOrTabPressed()
                        true
                    } else {
                        false
                    }
                },
            textStyle = TextStyle(
                color = if (isBookSelected) AppColors.Accent else AppColors.TextPrimary,
                fontSize = 16.sp
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isBookSelected) AppColors.Accent else AppColors.TextPrimary,
                unfocusedTextColor = if (isBookSelected) AppColors.Accent else AppColors.TextPrimary,
                cursorColor = AppColors.Accent,
                focusedBorderColor = if (isBookSelected) AppColors.Accent else AppColors.Accent,
                unfocusedBorderColor = if (isBookSelected) AppColors.Accent.copy(alpha = 0.5f) else AppColors.BorderColor,
                focusedLabelColor = AppColors.Accent,
                unfocusedLabelColor = AppColors.TextSecondary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        
        // 자동완성 제안 (책이 선택되지 않은 상태에서만 표시)
        if (!isBookSelected && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = AppColors.CardBackground,
                shadowElevation = 4.dp
            ) {
                Column {
                    suggestions.take(5).forEach { book ->
                        val fullName = BibleBookNames.toFullName(book.name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBook(book) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.TextPrimary
                            )
                        }
                        
                        if (book != suggestions.last()) {
                            HorizontalDivider(
                                color = AppColors.BorderColor.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
