package com.hoc081098.mapscompose.presentation.clustered

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.hoc081098.mapscompose.presentation.markers.MarkersUiState
import com.hoc081098.mapscompose.presentation.models.StoreUiModel
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import timber.log.Timber

class StoreClusterItem(private val store: StoreUiModel) : ClusterItem {
  override fun getPosition() = store.latLng.toGmsLatLng()
  override fun getTitle() = store.name
  override fun getSnippet() = store.description
  override fun getZIndex() = if (store.isFavorite) 5f else 2f
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
fun ClusteredMarkersScreen(
  uiState: MarkersUiState.Content,
) {
  val items = remember(uiState.stores) { uiState.stores.map(::StoreClusterItem) }
  Clustering(
    items = items,
    onClusterClick = {
      Timber.d("onClusterClick: $it")
      false
    },
    onClusterItemClick = {
      Timber.d("onClusterItemClick: $it")
      false
    },
  )
}

