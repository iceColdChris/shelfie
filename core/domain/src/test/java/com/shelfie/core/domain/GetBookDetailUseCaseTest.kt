package com.shelfie.core.domain

import com.shelfie.core.model.BookDetail
import com.shelfie.core.data.repository.BookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetBookDetailUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = GetBookDetailUseCase(repository)

    @Test
    fun `invoke delegates to repository getBookDetail`() = runTest {
        val detail = BookDetail.EMPTY.copy(id = "/works/OL1W", title = "Dune")
        coEvery { repository.getBookDetail("/works/OL1W") } returns detail

        val result = useCase("/works/OL1W")

        assertEquals(detail, result)
    }
}
