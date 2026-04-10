package com.shelfie.feature.books.domain.usecase

import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFavoriteBooksUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = GetFavoriteBooksUseCase(repository)

    @Test
    fun `invoke delegates to repository observeFavorites`() {
        val flow = flowOf(emptyList<Book>())
        every { repository.observeFavorites() } returns flow

        val result = useCase()

        assertEquals(flow, result)
    }
}
