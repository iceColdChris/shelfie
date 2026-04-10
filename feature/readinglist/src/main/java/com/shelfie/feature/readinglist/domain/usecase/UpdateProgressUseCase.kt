package com.shelfie.feature.readinglist.domain.usecase

import com.shelfie.feature.readinglist.domain.repository.ReadingListRepository
import javax.inject.Inject

/** Updates reading progress for a book (0-100). */
class UpdateProgressUseCase @Inject constructor(
    private val repository: ReadingListRepository
) {
    suspend operator fun invoke(bookId: String, progress: Int) =
        repository.updateProgress(bookId, progress.coerceIn(0, 100))
}
