package com.shelfie.feature.books.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shelfie.feature.books.domain.model.BookDetail
import com.valentinilk.shimmer.shimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.detail?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.detail?.let { detail ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (detail.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = if (detail.isFavorite) "Remove favorite" else "Add favorite",
                                tint = if (detail.isFavorite) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    ShimmerBookDetail(modifier = Modifier.fillMaxSize())
                }
                state.errorMessage != null -> {
                    ErrorState(
                        message = state.errorMessage ?: "Something went wrong.",
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.detail != null -> {
                    BookDetailContent(
                        detail = state.detail!!,
                        currentShelf = state.currentShelf,
                        onAddToShelf = { viewModel.showShelfPicker() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Shelf picker bottom sheet
    if (state.showShelfPicker) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissShelfPicker() },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Add to shelf",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                listOf(
                    "WANT_TO_READ" to "Want to Read",
                    "CURRENTLY_READING" to "Currently Reading",
                    "FINISHED" to "Finished"
                ).forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .let { mod ->
                                mod
                            },
                        trailingContent = {
                            if (state.currentShelf == value) {
                                Text(
                                    "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    Button(
                        onClick = { viewModel.addToShelf(value) },
                        enabled = state.currentShelf != value,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(if (state.currentShelf == value) "Already here" else "Add")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Retry")
        }
    }
}

@Composable
private fun ShimmerBookDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shimmer()
            .padding(horizontal = 16.dp)
            .clearAndSetSemantics { contentDescription = "Loading book details" }
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Cover placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        Spacer(modifier = Modifier.height(20.dp))
        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(24.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Author placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(18.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Description placeholders
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (it == 4) 0.5f else 1f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailContent(
    detail: BookDetail,
    currentShelf: String?,
    onAddToShelf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Cover image hero
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            if (detail.thumbnail != null) {
                AsyncImage(
                    model = detail.thumbnail,
                    contentDescription = detail.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .clip(MaterialTheme.shapes.large)
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "No cover image available",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(vertical = 48.dp)
                        .size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title + Authors
        Text(
            text = detail.title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (detail.authors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail.authors.joinToString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add to shelf button
        val shelfLabel = when (currentShelf) {
            "WANT_TO_READ" -> "On shelf: Want to Read"
            "CURRENTLY_READING" -> "On shelf: Currently Reading"
            "FINISHED" -> "On shelf: Finished"
            else -> null
        }
        FilledTonalButton(
            onClick = onAddToShelf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.LibraryAdd,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(shelfLabel ?: "Add to shelf")
        }

        // Description section
        detail.description?.let { desc ->
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SectionHeader(title = "About this book")
            Spacer(modifier = Modifier.height(8.dp))

            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Subjects section
        if (detail.subjects.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SectionHeader(title = "Subjects")
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                detail.subjects.forEach { subject ->
                    SuggestionChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(subject, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.semantics { heading() }
    )
}
