package com.hoc081098.mapscompose.presentation.markers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.hoc081098.mapscompose.R
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.presentation.utils.bitmapDescriptorFactory

object GoogleMapContentDefaults {
  val LatLng = LatLng(16.047079, 108.206230)
  const val ZoomLevel = 15f
  const val AnimateDurationMillis = 200
}

@Composable
fun GoogleMapContent(
  uiState: MarkersUiState.Content,
  modifier: Modifier,
  markerContent: @Composable @GoogleMapComposable (content: MarkersUiState.Content) -> Unit = {},
) {
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(
      /* target = */ GoogleMapContentDefaults.LatLng,
      /* zoom = */ GoogleMapContentDefaults.ZoomLevel
    )
  }

  LaunchedEffect(cameraPositionState, uiState.currentLatLng, uiState.zoomLevel) {
    cameraPositionState.animate(
      update = CameraUpdateFactory.newCameraPosition(
        CameraPosition.fromLatLngZoom(
          /* target = */ uiState.currentLatLng.toGmsLatLng(),
          /* zoom = */ if (uiState.zoomLevel.isNaN()) {
            cameraPositionState.position.zoom
          } else {
            uiState.zoomLevel
          }
        )
      ),
      durationMs = GoogleMapContentDefaults.AnimateDurationMillis,
    )
  }

  GoogleMap(
    modifier = modifier.fillMaxSize(),
    cameraPositionState = cameraPositionState,
    onMapClick = {},
    onMapLongClick = {},
  ) {
    markerContent(uiState)
  }
}

@Composable
fun rememberCurrentLocationBitmapDescriptor(): BitmapDescriptor? {
  val context = LocalContext.current
  val density = LocalDensity.current

  return remember(context, density) {
    val currentIconSizeInDp = density.run { 64.dp.toPx() }.toInt()

    context.bitmapDescriptorFactory(
      resId = R.drawable.ic_current_location_96,
      width = currentIconSizeInDp,
      height = currentIconSizeInDp,
    )
  }
}
