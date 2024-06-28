package com.hoc081098.mapscompose.presentation.markers

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.presentation.utils.CollectWithLifecycleEffect
import timber.log.Timber

private val LocationPermissions = listOf(
  Manifest.permission.ACCESS_COARSE_LOCATION,
  Manifest.permission.ACCESS_FINE_LOCATION,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkersScreen(
  title: String,
  modifier: Modifier = Modifier,
  viewModel: MarkersViewModel = viewModel(factory = MarkersViewModel.factory),
  markerContent: @Composable @GoogleMapComposable (content: MarkersUiState.Content) -> Unit,
) {
  val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

  LifecycleResumeEffect(viewModel) {
    viewModel.getCurrentLocationAndStores()
    onPauseOrDispose { }
  }

  val checkLocationSettingsLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    Timber.d("checkLocationSettingsLauncher: result=$result")
    viewModel.onGpsSettingsResult(enabled = result.resultCode == Activity.RESULT_OK)
  }

  val requestMultipleLocationPermissionsEffect =
    rememberRequestMultipleLocationPermissionsEffect(onPermissionsResult = viewModel::onPermissionResult)

  viewModel.singleEventFlow.CollectWithLifecycleEffect { event ->
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
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(text = title)
        }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding),
    ) {
      when (val s = uiState) {
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
            uiState = s,
            markerContent = markerContent,
          )
        }
      }
    }
  }
}

