package com.hoc081098.mapscompose.presentation.advanced

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.rememberMarkerState
import com.hoc081098.mapscompose.R
import com.hoc081098.mapscompose.presentation.markers.MarkersUiState
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.presentation.utils.BitmapParameters
import com.hoc081098.mapscompose.presentation.utils.vectorToBitmap

@Composable
@GoogleMapComposable
fun AdvancedMarkersScreen(
  uiState: MarkersUiState.Content,
) {
  val normalPin = with(PinConfig.builder()) {
    val storeIcon = vectorToBitmap(
      BitmapParameters(
        id = R.drawable.ic_store_100,
        iconColor = MaterialTheme.colorScheme.onSecondary.toArgb(),
      )
    )
    setGlyph(PinConfig.Glyph(storeIcon))
    setBackgroundColor(MaterialTheme.colorScheme.secondary.toArgb())
    setBorderColor(MaterialTheme.colorScheme.onSecondary.toArgb())
    build()
  }

  val favoritePin = with(PinConfig.builder()) {
    val storeIcon = vectorToBitmap(
      BitmapParameters(
        id = R.drawable.ic_store_96,
        iconColor = MaterialTheme.colorScheme.onSecondary.toArgb(),
      )
    )
    setGlyph(PinConfig.Glyph(storeIcon))
    setBackgroundColor(MaterialTheme.colorScheme.primary.toArgb())
    setBorderColor(MaterialTheme.colorScheme.onPrimary.toArgb())
    build()
  }

  val currentLocationPin = with(PinConfig.builder()) {
    val locationIcon = vectorToBitmap(
      BitmapParameters(
        id = R.drawable.baseline_location_on_24,
        iconColor = MaterialTheme.colorScheme.onSecondary.toArgb(),
      )
    )
    setGlyph(PinConfig.Glyph(locationIcon))
    setBackgroundColor(MaterialTheme.colorScheme.primary.toArgb())
    setBorderColor(MaterialTheme.colorScheme.onPrimary.toArgb())
    build()
  }

  uiState.stores.forEach { store ->
    key(store.id) {
      AdvancedMarker(
        state = rememberMarkerState(
          position = remember(store.latLng) {
            store.latLng.toGmsLatLng()
          }
        ),
        title = store.name,
        collisionBehavior = AdvancedMarkerOptions.CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL,
        pinConfig = if (store.isFavorite) favoritePin else normalPin,
        onClick = { false },
        anchor = Offset(x = 0.5f, y = 0.5f),
        snippet = store.description,
        zIndex = if (store.isFavorite) 5f else 2f,
        tag = store.id,
      )
    }
  }
  AdvancedMarker(
    state = rememberMarkerState(
      position = remember(uiState.currentLatLng) {
        uiState.currentLatLng.toGmsLatLng()
      }
    ),
    title = "Current location",
    collisionBehavior = AdvancedMarkerOptions.CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL,
    pinConfig = currentLocationPin,
    onClick = { false },
    anchor = Offset(x = 0.5f, y = 0.5f),
    snippet = "Current location",
    zIndex = 10f,
  )
}

