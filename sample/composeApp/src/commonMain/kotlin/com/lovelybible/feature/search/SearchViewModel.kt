package com.lovelybible.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lovelybible.domain.model.BibleBookNames
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.BiblePosition
import com.lovelybible.domain.repository.BibleRepository
import com.lovelybible.feature.navigation.NavigationIntent
import com.lovelybible.feature.navigation.NavigationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 검색 화면 ViewModel (MVI 패턴)
 */
class SearchViewModel(
    private val repository: BibleRepository,
    private val navigationViewModel: NavigationViewModel
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // State
    var state by mutableStateOf(SearchState())
        private set
    
    // Effect SharedFlow (여러 collector 지원)
    private val _effect = MutableSharedFlow<SearchEffect>(extraBufferCapacity = 10)
    val effect = _effect.asSharedFlow()
    
    /**
     * Intent 처리
     */
    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateBookQuery -> updateBookQuery(intent.query)
            is SearchIntent.UpdateChapter -> updateChapter(intent.chapter)
            is SearchIntent.UpdateVerse -> updateVerse(intent.verse)
            is SearchIntent.SelectBook -> selectBook(intent.book, intent.autoSearch)
            SearchIntent.ExecuteSearch -> executeSearch()
            SearchIntent.ClearSearch -> clearSearch()
            SearchIntent.ClearBookSelection -> clearBookSelection()
            is SearchIntent.SelectRecentSearch -> selectRecentSearch(intent.position)
            SearchIntent.ClearRecentSearches -> clearRecentSearches()
        }
    }
    
    private fun clearRecentSearches() {
        state = state.copy(recentSearches = emptyList())
    }
    
    private fun updateBookQuery(query: String) {
        state = state.copy(bookQuery = query)
        
        if (query.isBlank()) {
            state = state.copy(suggestions = emptyList())
            return
        }
        
        scope.launch {
            try {
                val suggestions = repository.searchBooks(query)
                state = state.copy(suggestions = suggestions)
            } catch (e: Exception) {
                state = state.copy(error = "책 검색 실패: ${e.message}")
            }
        }
    }
    
    private fun updateChapter(chapter: String) {
        state = state.copy(chapterInput = chapter)
    }
    
    private fun updateVerse(verse: String) {
        state = state.copy(verseInput = verse)
    }
    
    private fun selectBook(book: Book, autoSearch: Boolean = false) {
        val fullName = BibleBookNames.toFullName(book.name)
        
        if (autoSearch) {
            // 버튼 클릭 시: 1:1 자동 검색
            state = state.copy(
                selectedBook = book,
                bookQuery = fullName,
                chapterInput = "1",
                verseInput = "1",
                suggestions = emptyList()
            )
            
            // 즉시 검색 실행
            scope.launch {
                executeSearch(isAutoSearch = true)
            }
        } else {
            // Enter/Tab 입력 시: 책만 선택
            state = state.copy(
                selectedBook = book,
                bookQuery = fullName,
                suggestions = emptyList()
            )
        }
    }
    
    // 마지막 자동 검색 위치 (1:1 제거용)
    private var lastAutoSearchPosition: BiblePosition? = null
    
    private fun executeSearch(isAutoSearch: Boolean = false) {
        val book = state.selectedBook
        val chapter = state.chapterInput.toIntOrNull()
        val verse = state.verseInput.toIntOrNull()
        
        if (book == null || chapter == null || verse == null) {
            scope.launch {
                _effect.emit(SearchEffect.ShowError("책, 장, 절을 모두 입력해주세요"))
            }
            return
        }
        
        scope.launch {
            try {
                state = state.copy(isSearching = true)
                
                // 구절 존재 여부 확인
                val verseData = repository.getVerse(book.name, chapter, verse)
                
                if (verseData != null) {
                    val position = BiblePosition(book.name, chapter, verse)
                    
                    // NavigationViewModel을 통해 구절 표시
                    navigationViewModel.onIntent(NavigationIntent.NavigateToPosition(position))
                    
                    // 최근 검색 처리
                    var recentList = state.recentSearches.toMutableList()
                    
                    // 버튼 클릭 후 다른 구절 검색 시 이전 1:1 제거
                    if (!isAutoSearch && lastAutoSearchPosition != null) {
                        val autoPos = lastAutoSearchPosition!!
                        if (position.book == autoPos.book && 
                            (position.chapter != autoPos.chapter || position.verse != autoPos.verse)) {
                            // 같은 책이지만 다른 구절 → 1:1 제거
                            recentList.removeAll { 
                                it.book == autoPos.book && it.chapter == autoPos.chapter && it.verse == autoPos.verse 
                            }
                        }
                        lastAutoSearchPosition = null
                    }
                    
                    // 자동 검색 위치 기록
                    if (isAutoSearch) {
                        lastAutoSearchPosition = position
                    }
                    
                    // 최근 검색에 추가 (중복 제거 및 최대 10개 유지)
                    val updatedRecent = (listOf(position) + recentList)
                        .distinctBy { "${it.book}-${it.chapter}-${it.verse}" }
                        .take(10)
                    
                    if (isAutoSearch) {
                        // 버튼 클릭 자동 검색 후: 장/절 필드 비움
                        state = state.copy(
                            chapterInput = "",
                            verseInput = "",
                            suggestions = emptyList(),
                            recentSearches = updatedRecent,
                            isSearching = false
                        )
                    } else {
                        // 일반 검색 후: 모든 필드 초기화
                        state = state.copy(
                            selectedBook = null,
                            bookQuery = "",
                            chapterInput = "",
                            verseInput = "",
                            suggestions = emptyList(),
                            recentSearches = updatedRecent,
                            isSearching = false
                        )
                    }
                    
                    // 성경 화면으로 포커스 이동
                    _effect.emit(SearchEffect.FocusDisplayPanel)
                } else {
                    _effect.emit(SearchEffect.ShowError("해당 구절을 찾을 수 없습니다"))
                    state = state.copy(isSearching = false)
                }
            } catch (e: Exception) {
                _effect.emit(SearchEffect.ShowError("검색 실패: ${e.message}"))
                state = state.copy(isSearching = false)
            }
        }
    }
    
    private fun clearSearch() {
        state = SearchState()
    }
    
    /**
     * 책 선택만 해제 (수정 모드로 전환)
     * 쿼리는 유지하고 selectedBook만 null로 설정
     */
    private fun clearBookSelection() {
        state = state.copy(selectedBook = null)
    }
    
    private fun selectRecentSearch(position: BiblePosition) {
        scope.launch {
            try {
                // 책 정보 조회
                val books = repository.getAllBooks()
                val book = books.find { it.name == position.book }
                
                if (book != null) {
                    val fullName = BibleBookNames.toFullName(book.name)
                    state = state.copy(
                        selectedBook = book,
                        bookQuery = fullName,
                        chapterInput = position.chapter.toString(),
                        verseInput = position.verse.toString()
                    )
                    
                    executeSearch()
                }
            } catch (e: Exception) {
                _effect.emit(SearchEffect.ShowError("최근 검색 불러오기 실패"))
            }
        }
    }
}
