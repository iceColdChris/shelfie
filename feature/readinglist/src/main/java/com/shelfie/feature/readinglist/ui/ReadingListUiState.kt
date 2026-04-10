package com.shelfie.feature.readinglist.ui

import androidx.compose.runtime.Immutable
import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ReadingListUiState(
    val selectedShelf: Shelf = Shelf.WANT_TO_READ,
    val books: ImmutableList<ReadingListBook> = persistentListOf()
)
