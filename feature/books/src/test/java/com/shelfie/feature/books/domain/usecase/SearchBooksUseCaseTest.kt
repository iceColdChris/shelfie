package com.shelfie.feature.books.domain.usecase

import com.shelfie.feature.books.domain.model.Book
import com.shelfie.feature.books.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchBooksUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = SearchBooksUseCase(repository)

    @Test
    fun `observe delegates to repository observeSearchResults`() {
        val flow = flowOf(emptyList<Book>())
        every { repository.observeSearchResults("kotlin") } returns flow

        val result = useCase.observe("kotlin")

        assertEquals(flow, result)
    }

    @Test
    fun `refresh delegates to repository refreshSearch`() = runTest {
        coEvery { repository.refreshSearch("kotlin") } returns Unit

        useCase.refresh("kotlin")

        coVerify(exactly = 1) { repository.refreshSearch("kotlin") }
    }
}
