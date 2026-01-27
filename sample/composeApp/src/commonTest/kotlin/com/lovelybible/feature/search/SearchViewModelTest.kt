package com.lovelybible.feature.search

import com.lovelybible.domain.model.BiblePosition
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse
import com.lovelybible.domain.repository.BibleRepository
import com.lovelybible.feature.navigation.NavigationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 3.2 - SearchViewModel 테스트
 * - 책 쿼리 입력 시 suggestions 업데이트
 * - 검색 실행 시 NavigateToVerse effect 발생
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    /**
     * Fake Repository for testing
     */
    private class FakeBibleRepository : BibleRepository {
        val books = listOf(
            Book(1, "창", Testament.OLD, 50),
            Book(2, "출", Testament.OLD, 40),
            Book(3, "마", Testament.NEW, 28)
        )
        
        val verses = mapOf(
            Triple("창", 1, 1) to Verse("창", 1, 1, "태초에"),
            Triple("창", 1, 2) to Verse("창", 1, 2, "땅이")
        )
        
        override suspend fun getAllBooks() = books
        override suspend fun getOldTestamentBooks() = books.filter { it.testament == Testament.OLD }
        override suspend fun getNewTestamentBooks() = books.filter { it.testament == Testament.NEW }
        override suspend fun getVerses(book: String, chapter: Int) = 
            verses.values.filter { it.bookName == book && it.chapter == chapter }
        override suspend fun getVerse(book: String, chapter: Int, verse: Int) = 
            verses[Triple(book, chapter, verse)]
        override suspend fun getChapterCount(book: String) = books.find { it.name == book }?.chapterCount ?: 0
        override suspend fun getVerseCount(book: String, chapter: Int) = 
            verses.values.count { it.bookName == book && it.chapter == chapter }
        override suspend fun searchBooks(query: String) = 
            if (query.isBlank()) emptyList() else books.filter { it.name.contains(query) }
    }
    
    /**
     * 책 쿼리 입력 시 suggestions 업데이트 테스트
     */
    @Test
    fun testUpdateBookQuery_updatesSuggestions() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        // 초기 상태 확인
        assertTrue(viewModel.state.suggestions.isEmpty(), "초기 suggestions는 비어있어야 함")
        
        // 책 쿼리 입력
        viewModel.onIntent(SearchIntent.UpdateBookQuery("창"))
        
        // 비동기 작업 완료 대기
        advanceUntilIdle()
        
        // suggestions 업데이트 확인
        assertEquals("창", viewModel.state.bookQuery)
        assertTrue(viewModel.state.suggestions.isNotEmpty(), "suggestions가 업데이트되어야 함")
        assertTrue(viewModel.state.suggestions.any { it.name == "창" }, "검색 결과에 '창'이 있어야 함")
    }
    
    /**
     * 빈 쿼리 입력 시 suggestions 초기화 테스트
     */
    @Test
    fun testUpdateBookQuery_emptyQuery_clearsSuggestions() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        // 먼저 검색어 입력
        viewModel.onIntent(SearchIntent.UpdateBookQuery("창"))
        advanceUntilIdle()
        
        // 검색어 지우기
        viewModel.onIntent(SearchIntent.UpdateBookQuery(""))
        
        // suggestions가 비워져야 함
        assertTrue(viewModel.state.suggestions.isEmpty(), "빈 쿼리 시 suggestions가 비어야 함")
    }
    
    /**
     * 책 선택 테스트
     */
    @Test
    fun testSelectBook() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        val book = repository.books[0]
        
        // 책 선택
        viewModel.onIntent(SearchIntent.SelectBook(book))
        advanceUntilIdle()
        
        // 상태 확인
        assertEquals(book, viewModel.state.selectedBook, "선택된 책이 설정되어야 함")
        assertEquals("창세기", viewModel.state.bookQuery, "bookQuery가 책 이름으로 설정되어야 함")
        assertTrue(viewModel.state.suggestions.isEmpty(), "suggestions가 비워져야 함")
    }
    
    /**
     * 장/절 입력 테스트
     */
    @Test
    fun testUpdateChapterAndVerse() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        viewModel.onIntent(SearchIntent.UpdateChapter("5"))
        viewModel.onIntent(SearchIntent.UpdateVerse("10"))
        
        assertEquals("5", viewModel.state.chapterInput, "장 입력이 설정되어야 함")
        assertEquals("10", viewModel.state.verseInput, "절 입력이 설정되어야 함")
    }
    
    /**
     * 검색 실행 테스트 - 성공 케이스
     */
    @Test
    fun testExecuteSearch_success() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        val book = repository.books[0]
        
        // 검색 조건 설정
        viewModel.onIntent(SearchIntent.SelectBook(book))
        viewModel.onIntent(SearchIntent.UpdateChapter("1"))
        viewModel.onIntent(SearchIntent.UpdateVerse("1"))
        
        // 검색 실행
        viewModel.onIntent(SearchIntent.ExecuteSearch)
        advanceUntilIdle()
        
        // NavigationViewModel의 상태가 업데이트되었는지 확인
        val navState = navigationViewModel.state
        assertNotNull(navState.currentPosition, "현재 위치가 설정되어야 함")
        assertEquals("창", navState.currentPosition?.book)
        assertEquals(1, navState.currentPosition?.chapter)
        assertEquals(1, navState.currentPosition?.verse)
    }
    
    /**
     * 검색 초기화 테스트
     */
    @Test
    fun testClearSearch() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        // 상태 설정
        viewModel.onIntent(SearchIntent.SelectBook(repository.books[0]))
        viewModel.onIntent(SearchIntent.UpdateChapter("1"))
        viewModel.onIntent(SearchIntent.UpdateVerse("1"))
        advanceUntilIdle()
        
        // 초기화
        viewModel.onIntent(SearchIntent.ClearSearch)
        
        // 상태 확인
        assertEquals(SearchState(), viewModel.state, "상태가 초기화되어야 함")
    }
    
    /**
     * 최근 검색 선택 테스트
     */
    @Test
    fun testSelectRecentSearch() = runTest {
        val repository = FakeBibleRepository()
        val navigationViewModel = NavigationViewModel(repository)
        val viewModel = SearchViewModel(repository, navigationViewModel)
        
        val position = BiblePosition("창", 1, 1)
        
        // 최근 검색 선택
        viewModel.onIntent(SearchIntent.SelectRecentSearch(position))
        advanceUntilIdle()
        
        // 상태 확인
        val navState = navigationViewModel.state
        assertNotNull(navState.currentPosition, "현재 위치가 설정되어야 함")
    }
}
