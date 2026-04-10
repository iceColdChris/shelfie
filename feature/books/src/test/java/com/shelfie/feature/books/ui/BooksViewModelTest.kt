package com.shelfie.feature.books.ui

import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.usecase.GetFavoriteBooksUseCase
import com.shelfie.feature.books.domain.usecase.SearchBooksUseCase
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BooksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchBooksUseCase: SearchBooksUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val getFavoriteBooksUseCase: GetFavoriteBooksUseCase = mockk()

    private lateinit var viewModel: BooksViewModel

    @Before
    fun setUp() {
        viewModel = BooksViewModel(searchBooksUseCase, toggleFavoriteUseCase, getFavoriteBooksUseCase)
    }

    @Test
    fun `initial state is default BooksUiState`() {
        assertEquals(BooksUiState(), viewModel.uiState.value)
    }

    @Test
    fun `QueryChanged updates query in state`() {
        viewModel.onEvent(BooksUiEvent.QueryChanged("kotlin"))
        assertEquals("kotlin", viewModel.uiState.value.query)
    }

    @Test
    fun `ClearSearch resets query, books, hasSearched and errorMessage`() {
        viewModel.onEvent(BooksUiEvent.QueryChanged("dune"))
        viewModel.onEvent(BooksUiEvent.ClearSearch)

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertTrue(state.books.isEmpty())
        assertFalse(state.hasSearched)
        assertNull(state.errorMessage)
    }

    @Test
    fun `ToggleFavorite delegates to use case`() = runTest {
        val book = book()
        viewModel.onEvent(BooksUiEvent.ToggleFavorite(book))
        coVerify { toggleFavoriteUseCase(book) }
    }

    @Test
    fun `ToggleShowFavorites enters favorites mode and populates books`() = runTest {
        val favorites = listOf(book(id = "1", isFavorite = true))
        every { getFavoriteBooksUseCase() } returns flowOf(favorites)

        viewModel.onEvent(BooksUiEvent.ToggleShowFavorites)

        assertTrue(viewModel.uiState.value.showFavorites)
        assertEquals(favorites, viewModel.uiState.value.books.toList())
    }

    @Test
    fun `ToggleShowFavorites second call exits favorites mode`() = runTest {
        every { getFavoriteBooksUseCase() } returns flowOf(emptyList())

        viewModel.onEvent(BooksUiEvent.ToggleShowFavorites)
        viewModel.onEvent(BooksUiEvent.ToggleShowFavorites)

        assertFalse(viewModel.uiState.value.showFavorites)
    }

    @Test
    fun `Search event populates books and sets hasSearched on success`() = runTest {
        val books = listOf(book(id = "1"))
        every { searchBooksUseCase.observe("dune") } returns flowOf(books)
        coEvery { searchBooksUseCase.refresh("dune") } returns Unit

        viewModel.onEvent(BooksUiEvent.QueryChanged("dune"))
        viewModel.onEvent(BooksUiEvent.Search)

        val state = viewModel.uiState.value
        assertEquals(books, state.books.toList())
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
    }

    @Test
    fun `Search event sets errorMessage when refresh throws`() = runTest {
        every { searchBooksUseCase.observe("dune") } returns flowOf(emptyList())
        coEvery { searchBooksUseCase.refresh("dune") } throws RuntimeException("Network error")

        viewModel.onEvent(BooksUiEvent.QueryChanged("dune"))
        viewModel.onEvent(BooksUiEvent.Search)

        assertEquals("Network error", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `Search event does nothing for blank query`() = runTest {
        viewModel.onEvent(BooksUiEvent.Search)
        assertEquals(BooksUiState(), viewModel.uiState.value)
    }

    // ---- Helpers ----

    private fun book(id: String = "works/OL1W", isFavorite: Boolean = false) = Book(
        id = id,
        title = "Dune",
        authors = persistentListOf("Frank Herbert"),
        thumbnail = null,
        isFavorite = isFavorite,
    )
}
