package com.shelfie.feature.readinglist.ui

import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf

sealed interface ReadingListUiEvent {
    data class SelectShelf(val shelf: Shelf) : ReadingListUiEvent
    data class UpdateProgress(val bookId: String, val progress: Int) : ReadingListUiEvent
    data class RemoveBook(val bookId: String) : ReadingListUiEvent
    data class MoveToShelf(val bookId: String, val shelf: Shelf) : ReadingListUiEvent
    data class Reorder(val books: List<ReadingListBook>) : ReadingListUiEvent
}
