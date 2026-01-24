package com.lovelybible.feature.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lovelybible.domain.model.BiblePosition
import com.lovelybible.domain.model.Verse
import com.lovelybible.domain.repository.BibleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 내비게이션 ViewModel
 * 현재 위치 추적 및 구절 페이지네이션 관리
 * 장/책을 넘나드는 전체 성경 탐색 지원
 */
import com.lovelybible.domain.model.BibleBookNames

class NavigationViewModel(
    private val repository: BibleRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // 전체 책 목록 캐시
    private var allBooks: List<String> = emptyList()
    
    // State
    var state by mutableStateOf(NavigationState())
        private set
    
    init {
        // 앱 시작 시 책 목록 로드 및 기본 화면 설정
        scope.launch {
            try {
                allBooks = repository.getAllBooks().map { it.name }
                // navigateToPosition에서 자동으로 pageSize 설정
                navigateToPosition(BiblePosition("사신", 1, 1))
            } catch (e: Exception) {
                // 에러를 state에 반영하여 UI에 표시
                state = state.copy(
                    error = "책 목록을 불러올 수 없습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Intent 처리
     */
    fun onIntent(intent: NavigationIntent) {
        when (intent) {
            is NavigationIntent.NavigateToPosition -> navigateToPosition(intent.position)
            NavigationIntent.NavigatePrevious -> navigatePrevious()
            NavigationIntent.NavigateNext -> navigateNext()
            is NavigationIntent.UpdatePageSize -> updatePageSize(intent.size)
        }
    }
    
    /**
     * 특정 위치로 이동
     */
    private fun navigateToPosition(position: BiblePosition) {
        scope.launch {
            try {
                state = state.copy(isLoading = true, error = null)
                
                // 책 종류에 따라 pageSize 자동 설정
                val defaultPageSize = if (position.book == "사신") 5 else 1
                state = state.copy(pageSize = defaultPageSize)
                
                // 책 목록이 비어있으면 다시 로드
                if (allBooks.isEmpty()) {
                    allBooks = repository.getAllBooks().map { it.name }
                }
                
                // 해당 장의 모든 구절 조회
                val allVerses = repository.getVerses(position.book, position.chapter)
                
                if (allVerses.isEmpty()) {
                    state = state.copy(
                        isLoading = false,
                        error = "구절을 찾을 수 없습니다"
                    )
                    return@launch
                }
                
                // 현재 절부터 pageSize 만큼 구절 추출
                val startIndex = allVerses.indexOfFirst { it.verse == position.verse }
                if (startIndex == -1) {
                    state = state.copy(
                        isLoading = false,
                        error = "해당 절을 찾을 수 없습니다"
                    )
                    return@launch
                }
                
                // 하드코딩 제거 - 모든 책에서 state.pageSize 사용
                val displayedVerses = allVerses.drop(startIndex).take(state.pageSize)
                val fullBookName = BibleBookNames.toFullName(position.book)
                val title = "$fullBookName ${position.chapter}장"
                
                // 이전/다음 가능 여부 계산 (장/책 경계 고려)
                val canPrevious = canNavigatePrevious(position, startIndex)
                val canNext = canNavigateNext(position, startIndex, allVerses.size)
                
                state = state.copy(
                    currentPosition = position,
                    displayedVerses = displayedVerses,
                    currentTitle = title,
                    isLoading = false,
                    canNavigatePrevious = canPrevious,
                    canNavigateNext = canNext
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = "구절 로딩 실패: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 이전으로 이동 가능 여부 확인
     */
    private fun canNavigatePrevious(position: BiblePosition, currentIndex: Int): Boolean {
        // 현재 장 내에서 이동 가능
        if (currentIndex > 0) return true
        
        // 이전 장이 있으면 이동 가능
        if (position.chapter > 1) return true
        
        // 이전 책이 있으면 이동 가능
        val bookIndex = allBooks.indexOf(position.book)
        return bookIndex > 0
    }
    
    /**
     * 다음으로 이동 가능 여부 확인
     */
    private suspend fun canNavigateNext(position: BiblePosition, currentIndex: Int, versesCount: Int): Boolean {
        // 현재 장 내에서 이동 가능
        if (currentIndex + state.pageSize < versesCount) return true
        
        // 다음 장이 있는지 확인
        val chapterCount = repository.getChapterCount(position.book)
        if (position.chapter < chapterCount) return true
        
        // 다음 책이 있으면 이동 가능
        val bookIndex = allBooks.indexOf(position.book)
        return bookIndex < allBooks.size - 1
    }
    
    /**
     * 이전 페이지로 이동 (장/책 경계 넘어감)
     */
    private fun navigatePrevious() {
        val currentPos = state.currentPosition ?: return
        
        scope.launch {
            try {
                val allVerses = repository.getVerses(currentPos.book, currentPos.chapter)
                val currentIndex = allVerses.indexOfFirst { it.verse == currentPos.verse }
                
                when {
                    // 같은 장 내에서 이전으로
                    currentIndex > 0 -> {
                        val newIndex = maxOf(0, currentIndex - state.pageSize)
                        val newVerse = allVerses[newIndex]
                        navigateToPosition(BiblePosition(currentPos.book, currentPos.chapter, newVerse.verse))
                    }
                    // 이전 장으로
                    currentPos.chapter > 1 -> {
                        val prevChapter = currentPos.chapter - 1
                        val prevVerses = repository.getVerses(currentPos.book, prevChapter)
                        if (prevVerses.isNotEmpty()) {
                            // 이전 장의 마지막 페이지로
                            val lastPageStart = maxOf(0, prevVerses.size - state.pageSize)
                            val newVerse = prevVerses[lastPageStart]
                            navigateToPosition(BiblePosition(currentPos.book, prevChapter, newVerse.verse))
                        }
                    }
                    // 이전 책으로
                    else -> {
                        val bookIndex = allBooks.indexOf(currentPos.book)
                        if (bookIndex > 0) {
                            val prevBook = allBooks[bookIndex - 1]
                            val chapterCount = repository.getChapterCount(prevBook)
                            if (chapterCount > 0) {
                                val prevVerses = repository.getVerses(prevBook, chapterCount)
                                if (prevVerses.isNotEmpty()) {
                                    // 이전 책의 마지막 장의 마지막 페이지로
                                    val lastPageStart = maxOf(0, prevVerses.size - state.pageSize)
                                    val newVerse = prevVerses[lastPageStart]
                                    navigateToPosition(BiblePosition(prevBook, chapterCount, newVerse.verse))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                state = state.copy(error = "이전 페이지 이동 실패")
            }
        }
    }
    
    /**
     * 다음 페이지로 이동 (장/책 경계 넘어감)
     */
    private fun navigateNext() {
        val currentPos = state.currentPosition ?: return
        
        scope.launch {
            try {
                val allVerses = repository.getVerses(currentPos.book, currentPos.chapter)
                val currentIndex = allVerses.indexOfFirst { it.verse == currentPos.verse }
                
                when {
                    // 같은 장 내에서 다음으로
                    currentIndex != -1 && currentIndex + state.pageSize < allVerses.size -> {
                        val newIndex = currentIndex + state.pageSize
                        val newVerse = allVerses[newIndex]
                        navigateToPosition(BiblePosition(currentPos.book, currentPos.chapter, newVerse.verse))
                    }
                    // 다음 장으로
                    else -> {
                        val chapterCount = repository.getChapterCount(currentPos.book)
                        if (currentPos.chapter < chapterCount) {
                            val nextChapter = currentPos.chapter + 1
                            navigateToPosition(BiblePosition(currentPos.book, nextChapter, 1))
                        } else {
                            // 다음 책으로
                            val bookIndex = allBooks.indexOf(currentPos.book)
                            if (bookIndex < allBooks.size - 1) {
                                val nextBook = allBooks[bookIndex + 1]
                                navigateToPosition(BiblePosition(nextBook, 1, 1))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                state = state.copy(error = "다음 페이지 이동 실패")
            }
        }
    }
    
    /**
     * 페이지 크기 변경
     */
    private fun updatePageSize(size: Int) {
        if (size < 1 || size > 10) return
        
        state = state.copy(pageSize = size)
        
        // 현재 위치가 있으면 새 페이지 크기로 다시 로드
        state.currentPosition?.let { navigateToPosition(it) }
    }
}
