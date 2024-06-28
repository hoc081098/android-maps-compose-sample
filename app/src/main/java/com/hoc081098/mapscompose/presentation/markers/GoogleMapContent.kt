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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.DefaultMapProperties
import com.google.maps.android.compose.DefaultMapUiSettings
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.hoc081098.mapscompose.R
import com.hoc081098.mapscompose.presentation.models.LatLngUiModel
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.presentation.utils.bitmapDescriptorFactory
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

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      uiSettings = remember {
        DefaultMapUiSettings.copy(
          myLocationButtonEnabled = false,
        )
      },
      properties = remember {
        DefaultMapProperties.copy(
          isBuildingEnabled = true,
          isIndoorEnabled = true,
          maxZoomPreference = 25f,
        )
      },
      onMapClick = {},
      onMapLongClick = {},
      onMapLoaded = { isMapLoaded = true }
    ) {
      markerContent(uiState)
    }

    if (!isMapLoaded) {
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
