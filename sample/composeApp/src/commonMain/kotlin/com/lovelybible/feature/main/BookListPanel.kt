package com.lovelybible.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lovelybible.domain.model.BibleBookNames
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.repository.BibleRepository
import com.lovelybible.feature.search.SearchIntent
import com.lovelybible.feature.search.SearchViewModel
import com.lovelybible.theme.AppColors
import com.lovelybible.ui.components.GlassCard
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * 성경 책 목록 패널
 * 구약/신약 토글 및 책 그리드 표시
 */
@Composable
fun BookListPanel(
    searchViewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val repository: BibleRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var selectedTestament by remember { mutableStateOf(Testament.OLD) }
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // 책 목록 로드
    LaunchedEffect(selectedTestament) {
        isLoading = true
        error = null
        try {
            books = when (selectedTestament) {
                Testament.OLD -> repository.getOldTestamentBooks()
                Testament.NEW -> repository.getNewTestamentBooks()
            }
        } catch (e: Exception) {
            error = "책 목록을 불러올 수 없습니다: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            
            // 구약/신약 토글
            TestamentToggle(
                selectedTestament = selectedTestament,
                onTestamentSelected = { selectedTestament = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 책 그리드
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Accent)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "",
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                books.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "책 목록이 없습니다",
                            color = AppColors.TextSecondary
                        )
                    }
                }
                else -> {
                    BookGrid(
                        books = books,
                        onBookClick = { book ->
                            // 검색 필드에 책 이름 설정 및 선택
                            searchViewModel.onIntent(SearchIntent.SelectBook(book, autoSearch = true))
                        }
                    )
                }
            }
        }
    }
}

/**
 * 구약/신약 토글 버튼
 */
@Composable
private fun TestamentToggle(
    selectedTestament: Testament,
    onTestamentSelected: (Testament) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.CardBackground),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ToggleButton(
            text = "구약 (39권)",
            isSelected = selectedTestament == Testament.OLD,
            onClick = { onTestamentSelected(Testament.OLD) },
            modifier = Modifier.weight(1f)
        )
        
        ToggleButton(
            text = "신약 (27권)",
            isSelected = selectedTestament == Testament.NEW,
            onClick = { onTestamentSelected(Testament.NEW) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 토글 버튼 개별 아이템 - 헤더 역할
 */
@Composable
private fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(
                if (isSelected) AppColors.Accent else AppColors.CardBackground,
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) AppColors.Background else AppColors.TextSecondary
        )
    }
}

/**
 * 책 그리드
 */
@Composable
private fun BookGrid(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(books) { book ->
            BookItem(
                book = book,
                onClick = { onBookClick(book) }
            )
        }
    }
}

/**
 * 책 아이템
 */
@Composable
private fun BookItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fullName = BibleBookNames.toFullName(book.name)
    
    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = AppColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fullName,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
