package com.shelfie.feature.readinglist.ui

import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import com.shelfie.feature.readinglist.domain.usecase.GetShelfBooksUseCase
import com.shelfie.feature.readinglist.domain.usecase.RemoveFromShelfUseCase
import com.shelfie.feature.readinglist.domain.usecase.UpdateProgressUseCase
import com.shelfie.feature.readinglist.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReadingListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getShelfBooksUseCase: GetShelfBooksUseCase = mockk()
    private val removeFromShelfUseCase: RemoveFromShelfUseCase = mockk(relaxed = true)
    private val updateProgressUseCase: UpdateProgressUseCase = mockk(relaxed = true)
    private val readingListRepository: ReadingListRepository = mockk(relaxed = true)

    private lateinit var viewModel: ReadingListViewModel

    @Before
    fun setUp() {
        // Default: WANT_TO_READ shelf returns empty list (required by init block)
        every { getShelfBooksUseCase(Shelf.WANT_TO_READ) } returns flowOf(emptyList())
        viewModel = ReadingListViewModel(getShelfBooksUseCase, removeFromShelfUseCase, updateProgressUseCase, readingListRepository)
    }

    @Test
    fun `initial state observes WANT_TO_READ shelf`() {
        assertEquals(Shelf.WANT_TO_READ, viewModel.uiState.value.selectedShelf)
    }

    @Test
    fun `SelectShelf updates selectedShelf and populates books`() = runTest {
        val books = listOf(book(bookId = "b1"))
        every { getShelfBooksUseCase(Shelf.FINISHED) } returns flowOf(books)

        viewModel.onEvent(ReadingListUiEvent.SelectShelf(Shelf.FINISHED))

        assertEquals(Shelf.FINISHED, viewModel.uiState.value.selectedShelf)
        assertEquals(books, viewModel.uiState.value.books.toList())
    }

    @Test
    fun `SelectShelf replaces previously observed shelf`() = runTest {
        every { getShelfBooksUseCase(Shelf.CURRENTLY_READING) } returns flowOf(emptyList())
        every { getShelfBooksUseCase(Shelf.FINISHED) } returns flowOf(listOf(book()))

        viewModel.onEvent(ReadingListUiEvent.SelectShelf(Shelf.CURRENTLY_READING))
        viewModel.onEvent(ReadingListUiEvent.SelectShelf(Shelf.FINISHED))

        assertEquals(Shelf.FINISHED, viewModel.uiState.value.selectedShelf)
        assertEquals(1, viewModel.uiState.value.books.size)
    }

    @Test
    fun `UpdateProgress delegates to use case`() = runTest {
        viewModel.onEvent(ReadingListUiEvent.UpdateProgress(bookId = "b1", progress = 60))
        coVerify { updateProgressUseCase("b1", 60) }
    }

    @Test
    fun `RemoveBook delegates to use case`() = runTest {
        viewModel.onEvent(ReadingListUiEvent.RemoveBook(bookId = "b1"))
        coVerify { removeFromShelfUseCase("b1") }
    }

    @Test
    fun `MoveToShelf delegates to repository`() = runTest {
        viewModel.onEvent(ReadingListUiEvent.MoveToShelf(bookId = "b1", shelf = Shelf.FINISHED))
        coVerify { readingListRepository.moveToShelf("b1", Shelf.FINISHED) }
    }

    @Test
    fun `Reorder delegates to repository`() = runTest {
        val books = listOf(book(bookId = "a"), book(bookId = "b"))
        viewModel.onEvent(ReadingListUiEvent.Reorder(books))
        coVerify { readingListRepository.reorder(books) }
    }

    // ---- Helpers ----

    private fun book(bookId: String = "b1") = ReadingListBook(
        bookId = bookId,
        title = "Dune",
        authors = persistentListOf("F. Herbert"),
        thumbnail = null,
        shelf = Shelf.WANT_TO_READ,
        progress = 0,
        sortOrder = 0,
        addedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        finishedAt = null,
    )
}
