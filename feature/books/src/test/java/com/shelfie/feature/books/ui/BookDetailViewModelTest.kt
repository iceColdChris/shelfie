package com.shelfie.feature.books.ui

import androidx.lifecycle.SavedStateHandle
import com.shelfie.core.data.database.dao.ReadingListDao
import com.shelfie.core.data.database.entity.ReadingListEntry
import com.shelfie.feature.books.domain.model.BookDetail
import com.shelfie.feature.books.domain.usecase.GetBookDetailUseCase
import com.shelfie.feature.books.domain.usecase.ToggleFavoriteUseCase
import com.shelfie.feature.books.testutil.MainDispatcherRule
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

class BookDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle = SavedStateHandle(mapOf("bookId" to BOOK_ID))
    private val getBookDetailUseCase: GetBookDetailUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val readingListDao: ReadingListDao = mockk()

    private lateinit var viewModel: BookDetailViewModel

    private fun createViewModel() {
        viewModel = BookDetailViewModel(savedStateHandle, getBookDetailUseCase, toggleFavoriteUseCase, readingListDao)
    }

    @Test
    fun `successful load sets detail and clears isLoading`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

        createViewModel()

        val state = viewModel.uiState.value
        assertEquals(sampleDetail, state.detail)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `failed load sets errorMessage and clears isLoading`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } throws RuntimeException("Not found")
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

        createViewModel()

        val state = viewModel.uiState.value
        assertEquals("Not found", state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.detail)
    }

    @Test
    fun `toggleFavorite inverts isFavorite in detail state`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail.copy(isFavorite = false)
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.toggleFavorite()

        assertTrue(viewModel.uiState.value.detail?.isFavorite == true)
        coVerify { toggleFavoriteUseCase(any()) }
    }

    @Test
    fun `showShelfPicker sets showShelfPicker to true`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.showShelfPicker()

        assertTrue(viewModel.uiState.value.showShelfPicker)
    }

    @Test
    fun `dismissShelfPicker sets showShelfPicker to false`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

        createViewModel()
        viewModel.showShelfPicker()
        viewModel.dismissShelfPicker()

        assertFalse(viewModel.uiState.value.showShelfPicker)
    }

    @Test
    fun `observeShelfStatus reflects current shelf from dao`() = runTest {
        val entry = ReadingListEntry(
            bookId = BOOK_ID, title = "Dune", authors = listOf("F. Herbert"),
            thumbnail = null, shelf = "READING", addedAt = 0L,
        )
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(entry)

        createViewModel()

        assertEquals("READING", viewModel.uiState.value.currentShelf)
    }

    @Test
    fun `observeShelfStatus sets null shelf when no entry exists`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } returns sampleDetail
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

        createViewModel()

        assertNull(viewModel.uiState.value.currentShelf)
    }

    @Test
    fun `retry reloads detail after initial failure`() = runTest {
        coEvery { getBookDetailUseCase(BOOK_ID) } throws RuntimeException("Error") andThen sampleDetail
        every { readingListDao.observeEntry(BOOK_ID) } returns flowOf(null)

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
