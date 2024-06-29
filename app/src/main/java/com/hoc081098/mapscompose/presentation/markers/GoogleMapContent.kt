package com.hoc081098.mapscompose.presentation.markers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.hoc081098.mapscompose.BuildConfig
import com.hoc081098.mapscompose.R
import com.hoc081098.mapscompose.presentation.models.LatLngUiModel
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

object GoogleMapContentDefaults {
  val LatLng = LatLng(16.047079, 108.206230)
  const val ZoomLevel = 15f
  const val AnimateDurationMillis = 200
}

@Composable
fun GoogleMapContent(
  uiState: MarkersUiState.Content,
  cameraPositionState: CameraPositionState,
  modifier: Modifier,
  onDrag: () -> Unit = {},
  markerContent: @Composable @GoogleMapComposable (content: MarkersUiState.Content) -> Unit = {},
) {
  var isMapLoaded by remember { mutableStateOf(false) }

  HandleDragMapSideEffect(cameraPositionState, onDrag)

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

  val context = LocalContext.current
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      properties = remember {
        MapProperties(
          mapType = MapType.NORMAL,
          mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json)
        )
      },
      onMapClick = {},
      onMapLongClick = {},
      onMapLoaded = { isMapLoaded = true },
      googleMapOptionsFactory = { GoogleMapOptions().mapId(BuildConfig.MAPS_ID) }
    ) {
      markerContent(uiState)
    }

    AnimatedVisibility(
      modifier = Modifier.matchParentSize(),
      visible = !isMapLoaded,
      enter = EnterTransition.None,
      exit = fadeOut()
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
          .wrapContentSize()
      )
    }
  }
}

@Composable
private fun HandleDragMapSideEffect(
  cameraPositionState: CameraPositionState,
  onDrag: () -> Unit,
) {
  val onDragState = rememberUpdatedState(newValue = onDrag)

  LaunchedEffect(cameraPositionState, onDragState) {
    snapshotFlow { cameraPositionState.cameraMoveStartedReason to cameraPositionState.isMoving }
      .distinctUntilChanged()
      .collect { (reason, isMoving) ->
        Timber.tag("BuildGoogleMap").d("reason: $reason, isMoving=$isMoving")

        when (reason) {
          CameraMoveStartedReason.GESTURE -> {
            onDragState.value()
          }

          CameraMoveStartedReason.API_ANIMATION -> {
            Timber.tag("BuildGoogleMap").d("The user tapped something on the map.")
          }

          CameraMoveStartedReason.DEVELOPER_ANIMATION -> {
            Timber.tag("BuildGoogleMap").d("The app moved the camera.")
          }

          CameraMoveStartedReason.UNKNOWN,
          CameraMoveStartedReason.NO_MOVEMENT_YET -> Unit
        }
      }
  }
}

suspend fun CameraPositionState.animateToLatLngWithTheSameZoomLevel(latLng: LatLngUiModel) {
  animate(
    update = CameraUpdateFactory.newCameraPosition(
      CameraPosition.fromLatLngZoom(
        /* target = */ latLng.toGmsLatLng(),
        /* zoom = */ position.zoom
      )
    ),
    durationMs = GoogleMapContentDefaults.AnimateDurationMillis,
  )
}
