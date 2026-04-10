package com.shelfie.feature.stats.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shelfie.feature.stats.domain.usecase.GetReadingStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getReadingStatsUseCase: GetReadingStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        getReadingStatsUseCase()
            .onEach { stats ->
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }
}
