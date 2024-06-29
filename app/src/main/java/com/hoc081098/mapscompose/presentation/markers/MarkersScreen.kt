package com.hoc081098.mapscompose.presentation.markers

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.hoc081098.mapscompose.presentation.utils.CollectWithLifecycleEffect
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun MarkersScreen(
  modifier: Modifier = Modifier,
  onDrag: () -> Unit = {},
  viewModel: MarkersViewModel,
  markerContent: @Composable @GoogleMapComposable (content: MarkersUiState.Content) -> Unit,
) {
  val scope = rememberCoroutineScope()

  val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
  LifecycleResumeEffect(viewModel) {
    viewModel.getCurrentLocationAndStores()
    onPauseOrDispose { }
  }

  // -------------------------------------------- Single event handler --------------------------------------------

  val checkLocationSettingsLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    Timber.d("checkLocationSettingsLauncher: result=$result")
    viewModel.onGpsSettingsResult(enabled = result.resultCode == Activity.RESULT_OK)
  }

  val requestMultipleLocationPermissionsEffect =
    rememberRequestMultipleLocationPermissionsEffect(onPermissionsResult = viewModel::onPermissionResult)

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(
      /* target = */ GoogleMapContentDefaults.LatLng,
      /* zoom = */ GoogleMapContentDefaults.ZoomLevel
    )
  }

  viewModel.singleEventFlow.CollectWithLifecycleEffect { event ->
    Timber.d("singleEventFlow: event=$event")
    when (event) {
      is MarkersSingleEvent.LocationSettingsDisabled -> {
        checkLocationSettingsLauncher.launch(
          IntentSenderRequest
            .Builder(event.resolvableApiException.resolution)
            .build()
        )
      }

      MarkersSingleEvent.CheckLocationPermission -> {
        requestMultipleLocationPermissionsEffect()
      }

      MarkersSingleEvent.ZoomToCurrentLocation -> {
        val currentLatLng = (viewModel.uiStateFlow.value as? MarkersUiState.Content)
          ?.currentLatLng
          ?: return@CollectWithLifecycleEffect
        scope.launch { cameraPositionState.animateToLatLngWithTheSameZoomLevel(latLng = currentLatLng) }
      }
    }
  }

  // -------------------------------------------- UI --------------------------------------------

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    when (val s = uiState) {
      MarkersUiState.Uninitialized ->
        Unit

      MarkersUiState.Error -> {
        Text(
          modifier = Modifier.align(Alignment.Center),
          text = "Error",
          style = MaterialTheme.typography.titleMedium,
        )
      }

      MarkersUiState.Loading -> {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center)
        )
      }

      is MarkersUiState.Content -> {
        GoogleMapContent(
          modifier = Modifier.fillMaxSize(),
          cameraPositionState = cameraPositionState,
          uiState = s,
          markerContent = markerContent,
          onDrag = onDrag,
        )
      }
    }
  }
}

