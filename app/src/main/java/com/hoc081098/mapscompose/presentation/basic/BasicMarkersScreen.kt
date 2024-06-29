package com.hoc081098.mapscompose.presentation.basic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.hoc081098.mapscompose.presentation.markers.MarkersUiState
import com.hoc081098.mapscompose.presentation.models.StoreUiModel
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.presentation.utils.rememberStoreBitmapDescriptors

@Composable
@GoogleMapComposable
fun BasicMarkersScreen(
  uiState: MarkersUiState.Content,
) {
  var selectedStore by remember { mutableStateOf<StoreUiModel?>(null) }
  val onClickStore = { store: StoreUiModel -> selectedStore = store }
  val bitmapDescriptors = rememberStoreBitmapDescriptors()

  uiState.stores.forEach { store ->
    key(store.id) {
      Marker(
        state = rememberMarkerState(
          position = remember(store.latLng) {
            store.latLng.toGmsLatLng()
          }
        ),
        icon = if (selectedStore == store) {
          if (store.isFavorite) {
            bitmapDescriptors.selectedFavoriteStoreIcon
          } else {
            bitmapDescriptors.selectedNormalStoreIcon
          }
        } else {
          if (store.isFavorite) {
            bitmapDescriptors.unselectedFavoriteStoreIcon
          } else {
            bitmapDescriptors.unselectedNormalStoreIcon
          }
        },
        title = store.name,
        onClick = {
          onClickStore(store)
          false
        },
        anchor = Offset(x = 0.5f, y = 0.5f),
        snippet = store.description,
        zIndex = if (store.isFavorite) 5f else 2f,
        tag = store.id,
      )
    }
  }

  Marker(
    state = rememberMarkerState(
      position = remember(uiState.currentLatLng) {
        uiState.currentLatLng.toGmsLatLng()
      }
    ),
    icon = bitmapDescriptors.currentLocationIcon,
    title = "Current location",
    anchor = Offset(x = 0.5f, y = 0.5f),
    zIndex = 10f,
  )
}

