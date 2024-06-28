package com.hoc081098.mapscompose.presentation.markers

import androidx.compose.runtime.Immutable
import com.hoc081098.mapscompose.presentation.models.LatLngUiModel
import com.hoc081098.mapscompose.presentation.models.StoreUiModel
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface MarkersUiState {
  data object Uninitialized : MarkersUiState

  data object Loading : MarkersUiState

  data object Error : MarkersUiState

  data class Content(
    val currentLatLng: LatLngUiModel,
    val zoomLevel: Float,
    val stores: ImmutableList<StoreUiModel>,
    val isRefreshing: Boolean,
  ) : MarkersUiState
}
