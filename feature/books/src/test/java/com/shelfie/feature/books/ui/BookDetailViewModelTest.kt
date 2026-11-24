package com.shelfie.feature.books.ui

import com.shelfie.core.domain.AddToShelfUseCase
import com.shelfie.core.domain.GetShelfEntryUseCase
import com.shelfie.core.model.ReadingListBook
import com.shelfie.core.model.Shelf
import com.shelfie.core.model.BookDetail
import com.shelfie.core.domain.GetBookDetailUseCase
import com.shelfie.core.domain.ToggleFavoriteUseCase
import com.shelfie.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class BookDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getBookDetailUseCase: GetBookDetailUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val addToShelfUseCase: AddToShelfUseCase = mockk(relaxed = true)
    private val getShelfEntryUseCase: GetShelfEntryUseCase = mockk()

    private lateinit var viewModel: BookDetailViewModel

    private fun createViewModel() {
        viewModel = BookDetailViewModel(
            BOOK_ID,
            getBookDetailUseCase,
            toggleFavoriteUseCase,
            addToShelfUseCase,
            getShelfEntryUseCase
        )
    }

    @Test
    fun `successful load sets detail and clears isLoading`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()

        val state = viewModel.uiState.value
        assertEquals(sampleDetail, state.detail)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `failed load sets errorMessage and clears isLoading`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } throws RuntimeException("Not found")
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()

        val state = viewModel.uiState.value
        assertEquals("Not found", state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.detail)
    }

    @Test
    fun `toggleFavorite inverts isFavorite in detail state`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail.copy(isFavorite = false)
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.toggleFavorite()

        assertTrue(viewModel.uiState.value.detail?.isFavorite == true)
        coVerify { toggleFavoriteUseCase(any()) }
    }

    @Test
    fun `showShelfPicker sets showShelfPicker to true`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.showShelfPicker()

        assertTrue(viewModel.uiState.value.showShelfPicker)
    }

    @Test
    fun `dismissShelfPicker sets showShelfPicker to false`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.showShelfPicker()
        viewModel.dismissShelfPicker()

        assertFalse(viewModel.uiState.value.showShelfPicker)
    }

    @Test
    fun `observeShelfStatus reflects current shelf of the entry`() = runTest {
        val entry = ReadingListBook(
            bookId = BOOK_ID, title = "Dune", authors = persistentListOf("F. Herbert"),
            thumbnail = null, shelf = Shelf.CURRENTLY_READING, progress = 0, sortOrder = 0,
            addedAt = Instant.fromEpochMilliseconds(0), finishedAt = null,
        )
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(entry)

        createViewModel()

        assertEquals("CURRENTLY_READING", viewModel.uiState.value.currentShelf)
    }

    @Test
    fun `addToShelf adds the book to the chosen shelf and closes the picker`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.showShelfPicker()
        viewModel.addToShelf("FINISHED")

        coVerify {
            addToShelfUseCase(BOOK_ID, "Dune", sampleDetail.authors, null, Shelf.FINISHED)
        }
        assertFalse(viewModel.uiState.value.showShelfPicker)
    }

    @Test
    fun `observeShelfStatus sets null shelf when no entry exists`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()

        assertNull(viewModel.uiState.value.currentShelf)
    }

    @Test
    fun `retry reloads detail after initial failure`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } throws RuntimeException("Error") andThen sampleDetail
        every { getShelfEntryUseCase(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.retry()

        val state = viewModel.uiState.value
        assertEquals(sampleDetail, state.detail)
        assertNull(state.errorMessage)
    }

    // ---- Helpers ----

    companion object {
        private const val BOOK_ID = "/works/OL123W"

        private val sampleDetail = BookDetail(
            id = BOOK_ID,
            title = "Dune",
            authors = persistentListOf("Frank Herbert"),
            thumbnail = null,
            description = "A science fiction epic.",
            subjects = persistentListOf("Science Fiction"),
            isFavorite = false,
        )
    }
}
