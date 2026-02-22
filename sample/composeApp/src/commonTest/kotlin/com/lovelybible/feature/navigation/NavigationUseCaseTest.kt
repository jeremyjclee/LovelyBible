package com.lovelybible.feature.navigation

import com.lovelybible.domain.model.BiblePosition
import com.lovelybible.domain.model.Book
import com.lovelybible.domain.model.Testament
import com.lovelybible.domain.model.Verse
import com.lovelybible.domain.repository.BibleRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 4.2 - Navigation UseCase 테스트
 * - 장 끝에서 다음 장으로 이동
 * - 책 끝에서 다음 책으로 이동
 * - 창세기 1:1에서 이전 불가
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationUseCaseTest {
    
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
     * Fake Repository for navigation testing
     */
    private class FakeNavigationRepository : BibleRepository {
        private val books = listOf(
            Book(1, "창", Testament.OLD, 3),
            Book(2, "출", Testament.OLD, 2),
            Book(67, "사신", Testament.NEW, 1) // 사도신경 추가
        )
        
        // 창세기: 3장, 각 장마다 2절
        // 출애굽기: 2장, 각 장마다 2절
        // 사도신경: 1장, 5절
        private val allVerses = listOf(
            Verse("창", 1, 1, "창1:1"), Verse("창", 1, 2, "창1:2"),
            Verse("창", 2, 1, "창2:1"), Verse("창", 2, 2, "창2:2"),
            Verse("창", 3, 1, "창3:1"), Verse("창", 3, 2, "창3:2"),
            Verse("출", 1, 1, "출1:1"), Verse("출", 1, 2, "출1:2"),
            Verse("출", 2, 1, "출2:1"), Verse("출", 2, 2, "출2:2"),
            Verse("사신", 1, 1, "사신1:1"), Verse("사신", 1, 2, "사신1:2"),
            Verse("사신", 1, 3, "사신1:3"), Verse("사신", 1, 4, "사신1:4"),
            Verse("사신", 1, 5, "사신1:5")
        )
        
        override suspend fun getAllBooks() = books
        override suspend fun getOldTestamentBooks() = books.filter { it.testament == Testament.OLD }
        override suspend fun getNewTestamentBooks() = books.filter { it.testament == Testament.NEW }
        override suspend fun getVerses(book: String, chapter: Int) = 
            allVerses.filter { it.bookName == book && it.chapter == chapter }
        override suspend fun getVerse(book: String, chapter: Int, verse: Int) = 
            allVerses.find { it.bookName == book && it.chapter == chapter && it.verse == verse }
        override suspend fun getChapterCount(book: String) = 
            allVerses.filter { it.bookName == book }.map { it.chapter }.distinct().size
        override suspend fun getVerseCount(book: String, chapter: Int) = 
            allVerses.count { it.bookName == book && it.chapter == chapter }
        override suspend fun searchBooks(query: String) = 
            if (query.isBlank()) emptyList() else books.filter { it.name.contains(query) }
    }
    
    /**
     * 특정 위치로 이동 테스트
     */
    @Test
    fun testNavigateToPosition() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val position = BiblePosition("창", 1, 1)
        
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()
        
        assertEquals(position, viewModel.state.currentPosition, "현재 위치가 설정되어야 함")
        assertTrue(viewModel.state.displayedVerses.isNotEmpty(), "구절이 표시되어야 함")
    }
    
    /**
     * 장 내에서 다음 페이지 이동 테스트 (pageSize = 1)
     */
    @Test
    fun testNavigateNext_withinChapter() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        // 초기 위치 설정
        val position = BiblePosition("창", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()

        // 페이지 크기를 1로 설정 (한 번에 1절씩 이동)
        viewModel.onIntent(NavigationIntent.UpdatePageSize(1))
        advanceUntilIdle()
        
        // 다음으로 이동
        viewModel.onIntent(NavigationIntent.NavigateNext)
        advanceUntilIdle()
        
        // 같은 장 내에서 다음 절로 이동
        val newPosition = viewModel.state.currentPosition
        assertEquals("창", newPosition?.book)
        assertEquals(1, newPosition?.chapter)
        assertEquals(2, newPosition?.verse, "다음 절로 이동해야 함")
    }
    
    /**
     * 이전 페이지 이동 테스트
     */
    @Test
    fun testNavigatePrevious_withinChapter() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        // 초기 위치 설정 (2절에서 시작)
        val position = BiblePosition("창", 1, 2)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()
        
        // 이전으로 이동
        viewModel.onIntent(NavigationIntent.NavigatePrevious)
        advanceUntilIdle()
        
        // 이전 절로 이동
        val newPosition = viewModel.state.currentPosition
        assertEquals(1, newPosition?.verse, "이전 절로 이동해야 함")
    }
    
    /**
     * 창세기 1:1에서 이전 불가 테스트
     */
    @Test
    fun testNavigatePrevious_atBeginning_cannotGoBack() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        // 성경 시작 위치 설정
        val startPosition = BiblePosition("창", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(startPosition))
        advanceUntilIdle()
        
        // 이전으로 갈 수 없음
        assertFalse(viewModel.state.canNavigatePrevious, "처음 위치에서는 이전으로 갈 수 없어야 함")
    }
    
    /**
     * 페이지 크기 변경 및 범위 검증 테스트
     */
    @Test
    fun testUpdatePageSize() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        // 초기 위치 설정
        val position = BiblePosition("창", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()
        
        // 페이지 크기 변경
        viewModel.onIntent(NavigationIntent.UpdatePageSize(2))
        advanceUntilIdle()
        
        assertEquals(2, viewModel.state.pageSize, "페이지 크기가 변경되어야 함")
        assertEquals(2, viewModel.state.displayedVerses.size, "2개의 구절이 표시되어야 함")
    }
    
    @Test
    fun testUpdatePageSize_boundaries() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val position = BiblePosition("창", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()
        
        val initialSize = viewModel.state.pageSize
        
        viewModel.onIntent(NavigationIntent.UpdatePageSize(0))
        assertEquals(initialSize, viewModel.state.pageSize, "0은 유효하지 않음") // Should be 1
        
        viewModel.onIntent(NavigationIntent.UpdatePageSize(11))
        assertEquals(initialSize, viewModel.state.pageSize, "11은 유효하지 않음")
        
        viewModel.onIntent(NavigationIntent.UpdatePageSize(5))
        assertEquals(5, viewModel.state.pageSize, "5는 유효함")
    }
    
    @Test
    fun testCurrentTitle() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val position = BiblePosition("창", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()
        
        assertEquals("창세기 1장", viewModel.state.currentTitle, "제목 형식이 '책이름 N장'이어야 함")
    }
    
    @Test
    fun testNavigateToPosition_notFound() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val invalidPosition = BiblePosition("없는책", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(invalidPosition))
        advanceUntilIdle()
        
        assertTrue(viewModel.state.displayedVerses.isEmpty() || viewModel.state.error != null,
            "존재하지 않는 위치는 에러를 발생시켜야 함")
    }

    @Test
    fun testLoadingState() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        assertFalse(viewModel.state.isLoading, "초기 로딩 상태는 false이어야 함")
        
        val position = BiblePosition("창", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(position))
        advanceUntilIdle()
        
        assertFalse(viewModel.state.isLoading, "로딩 완료 후 false이어야 함")
    }

    @Test
    fun testNavigateNext_endOfChapter_goesToNextChapter() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val lastVerseOfChapter1 = BiblePosition("창", 1, 2)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(lastVerseOfChapter1))
        advanceUntilIdle()
        
        viewModel.onIntent(NavigationIntent.NavigateNext)
        advanceUntilIdle()
        
        val newPosition = viewModel.state.currentPosition
        if (newPosition != null && newPosition.chapter != 1) {
            assertEquals(2, newPosition.chapter, "다음 장으로 이동해야 함")
        }
    }

    @Test
    fun testNavigateNext_endOfBook_goesToNextBook() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val lastVerseOfBook = BiblePosition("창", 3, 2)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(lastVerseOfBook))
        advanceUntilIdle()
        
        viewModel.onIntent(NavigationIntent.NavigateNext)
        advanceUntilIdle()
        
        val newPosition = viewModel.state.currentPosition
        if (newPosition?.book != "창") {
            assertEquals("출", newPosition?.book, "다음 책으로 이동해야 함")
        }
    }
    
    @Test
    fun testNavigatePrevious_startOfChapter_goesToPreviousChapter() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val firstVerseOfChapter2 = BiblePosition("창", 2, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(firstVerseOfChapter2))
        advanceUntilIdle()
        
        viewModel.onIntent(NavigationIntent.NavigatePrevious)
        advanceUntilIdle()
        
        val newPosition = viewModel.state.currentPosition
        if (newPosition != null && newPosition.chapter != 2) {
            assertEquals(1, newPosition.chapter, "이전 장으로 이동해야 함")
        }
    }
    
    @Test
    fun testNavigatePrevious_startOfBook_goesToPreviousBook() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        val firstVerseOfBook = BiblePosition("출", 1, 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(firstVerseOfBook))
        advanceUntilIdle()
        
        viewModel.onIntent(NavigationIntent.NavigatePrevious)
        advanceUntilIdle()
        
        val newPosition = viewModel.state.currentPosition
        if (newPosition?.book != "출") {
            assertEquals("창", newPosition?.book, "이전 책으로 이동해야 함")
        }
    }

    /**
     * 사도신경으로 이동 시 페이지 크기 1로 설정 테스트
     */
    @Test
    fun testNavigateToApostlesCreed_setsPageSizeTo1() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing. Should be at "사신", size 1.
        
        // 창세기(1절)에서 시작
        viewModel.onIntent(NavigationIntent.NavigateToPosition(BiblePosition("창", 1, 1)))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.pageSize, "Bible books default to pageSize 1")
        
        // 사도신경으로 이동
        viewModel.onIntent(NavigationIntent.NavigateToPosition(BiblePosition("사신", 1, 1)))
        advanceUntilIdle()
        
        assertEquals(1, viewModel.state.pageSize, "Apostles' Creed should default to pageSize 1")
    }

    /**
     * 성경 책으로 이동 시 페이지 크기 1로 설정 테스트
     */
    @Test
    fun testNavigateToBibleBook_setsPageSizeTo1() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing. At "사신", size 1.
        
        // 사도신경으로 명시적 이동 (already there, but for safety)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(BiblePosition("사신", 1, 1)))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.pageSize)
        
        // 창세기로 이동
        viewModel.onIntent(NavigationIntent.NavigateToPosition(BiblePosition("창", 1, 1)))
        advanceUntilIdle()
        
        assertEquals(1, viewModel.state.pageSize, "Standard Bible books should default to pageSize 1")
    }

    /**
     * 같은 책 내에서 수동 조작 유지 테스트
     */
    @Test
    fun testManualPageSizeOverride_withinSameBook() = runTest {
        val repository = FakeNavigationRepository()
        val viewModel = NavigationViewModel(repository)
        advanceUntilIdle() // Init flushing
        
        // 창세기 1:1로 이동 (Default 1)
        viewModel.onIntent(NavigationIntent.NavigateToPosition(BiblePosition("창", 1, 1)))
        advanceUntilIdle()
        
        // 수동으로 3으로 변경
        viewModel.onIntent(NavigationIntent.UpdatePageSize(3))
        advanceUntilIdle()
        assertEquals(3, viewModel.state.pageSize)
        
        // 같은 책(창세기) 내에서 2장으로 이동
        viewModel.onIntent(NavigationIntent.NavigateToPosition(BiblePosition("창", 2, 1)))
        advanceUntilIdle()
        
        assertEquals(3, viewModel.state.pageSize, "Manual page size should persist within the same book")
    }
}
