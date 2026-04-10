package com.shelfie.feature.readinglist.domain.usecase

import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateProgressUseCaseTest {

    private val repository: ReadingListRepository = mockk()
    private val useCase = UpdateProgressUseCase(repository)

    @Test
    fun `passes normal value through unchanged`() = runTest {
        coEvery { repository.updateProgress(any(), any()) } returns Unit

        useCase("b1", 50)

        coVerify { repository.updateProgress("b1", 50) }
    }

    @Test
    fun `clamps negative value to 0`() = runTest {
        coEvery { repository.updateProgress(any(), any()) } returns Unit

        useCase("b1", -10)

        coVerify { repository.updateProgress("b1", 0) }
    }

    @Test
    fun `clamps value above 100 to 100`() = runTest {
        coEvery { repository.updateProgress(any(), any()) } returns Unit

        useCase("b1", 150)

        coVerify { repository.updateProgress("b1", 100) }
    }

    @Test
    fun `passes boundary value 0 through unchanged`() = runTest {
        coEvery { repository.updateProgress(any(), any()) } returns Unit

        useCase("b1", 0)

        coVerify { repository.updateProgress("b1", 0) }
    }

    @Test
    fun `passes boundary value 100 through unchanged`() = runTest {
        coEvery { repository.updateProgress(any(), any()) } returns Unit

        useCase("b1", 100)

        coVerify { repository.updateProgress("b1", 100) }
    }
}
