package com.shelfie.feature.books.domain.usecase

import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun `adds book to favorites when isFavorite is false`() = runTest {
        val book = book(isFavorite = false)
        coEvery { repository.addFavorite(book) } returns Unit

        useCase(book)

        coVerify(exactly = 1) { repository.addFavorite(book) }
        coVerify(exactly = 0) { repository.removeFavorite(any()) }
    }

    @Test
    fun `removes book from favorites when isFavorite is true`() = runTest {
        val book = book(isFavorite = true)
        coEvery { repository.removeFavorite(book.id) } returns Unit

        useCase(book)

        coVerify(exactly = 1) { repository.removeFavorite(book.id) }
        coVerify(exactly = 0) { repository.addFavorite(any()) }
    }

    private fun book(isFavorite: Boolean) = Book(
        id = "works/OL1W",
        title = "Dune",
        authors = persistentListOf("Frank Herbert"),
        thumbnail = null,
        isFavorite = isFavorite,
    )
}
