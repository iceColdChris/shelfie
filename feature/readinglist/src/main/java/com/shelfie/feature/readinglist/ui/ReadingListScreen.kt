package com.shelfie.feature.readinglist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shelfie.feature.readinglist.domain.model.ReadingListBook
import com.shelfie.feature.readinglist.domain.model.Shelf

@Composable
fun ReadingListScreen(
    onBookClick: (bookId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReadingListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReadingListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBookClick = onBookClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingListContent(
    state: ReadingListUiState,
    onEvent: (ReadingListUiEvent) -> Unit,
    onBookClick: (bookId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Library") },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = Shelf.entries.indexOf(state.selectedShelf)
            ) {
                Shelf.entries.forEach { shelf ->
                    Tab(
                        selected = state.selectedShelf == shelf,
                        onClick = { onEvent(ReadingListUiEvent.SelectShelf(shelf)) },
                        text = { Text(shelf.displayName, maxLines = 1) }
                    )
                }
            }

            if (state.books.isEmpty()) {
                EmptyShelfState(
                    shelf = state.selectedShelf,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = state.books,
                        key = { it.bookId },
                        contentType = { "reading_list_item" }
                    ) { book ->
                        SwipeToDismissItem(
                            book = book,
                            onClick = { onBookClick(book.bookId) },
                            onProgressChange = { progress ->
                                onEvent(ReadingListUiEvent.UpdateProgress(book.bookId, progress))
                            },
                            onDismiss = {
                                onEvent(ReadingListUiEvent.RemoveBook(book.bookId))
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissItem(
    book: ReadingListBook,
    onClick: () -> Unit,
    onProgressChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier.semantics {
            // Provide custom action so TalkBack users can remove without swiping
            customActions = listOf(
                CustomAccessibilityAction("Remove from shelf") {
                    onDismiss()
                    true
                }
            )
        }
    ) {
        ReadingListItem(
            book = book,
            onClick = onClick,
            onProgressChange = onProgressChange
        )
    }
}

@Composable
private fun ReadingListItem(
    book: ReadingListBook,
    onClick: () -> Unit,
    onProgressChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressDesc = if (book.shelf == Shelf.CURRENTLY_READING) ", ${book.progress}% complete" else ""
    val cardDesc = "${book.title} by ${book.authors.joinToString()}$progressDesc"

    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = cardDesc
            }
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
        ) {
            // Thumbnail
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 50.dp, height = 72.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                if (book.thumbnail != null) {
                    AsyncImage(
                        model = book.thumbnail,
                        contentDescription = null, // merged into card
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (book.authors.isNotEmpty()) {
                    Text(
                        text = book.authors.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Progress for "Currently Reading"
                if (book.shelf == Shelf.CURRENTLY_READING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    var sliderValue by remember(book.bookId, book.progress) {
                        mutableFloatStateOf(book.progress.toFloat())
                    }
                    Text(
                        text = "${sliderValue.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onProgressChange(sliderValue.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier
                            .height(24.dp)
                            .semantics {
                                stateDescription = "Reading progress: ${sliderValue.toInt()} percent"
                            }
                    )
                }

                // Progress bar for all shelves (except "Want to Read" with 0%)
                if (book.shelf != Shelf.CURRENTLY_READING && book.progress > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { book.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyShelfState(
    shelf: Shelf,
    modifier: Modifier = Modifier
) {
    val message = when (shelf) {
        Shelf.WANT_TO_READ -> "Books you want to read will appear here.\nSearch for a book and add it to this shelf."
        Shelf.CURRENTLY_READING -> "Books you're reading now will appear here.\nMove a book from \"Want to Read\" to start tracking."
        Shelf.FINISHED -> "Books you've finished will appear here.\nMark a book as finished to track your progress."
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(48.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
