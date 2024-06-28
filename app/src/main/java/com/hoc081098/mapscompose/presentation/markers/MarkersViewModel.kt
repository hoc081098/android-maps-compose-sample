package com.hoc081098.mapscompose.presentation.markers

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.hoc081098.mapscompose.MapsComposeAppLocator
import com.hoc081098.mapscompose.domain.StoreRepository
import com.hoc081098.mapscompose.presentation.AndroidLocationManager
import com.hoc081098.mapscompose.presentation.AndroidLocationManager.LocationSettingsError
import com.hoc081098.mapscompose.presentation.models.PermissionStatus
import com.hoc081098.mapscompose.presentation.models.toUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

sealed interface MarkersSingleEvent {
  data object CheckLocationPermission : MarkersSingleEvent

  data class LocationSettingsDisabled(val resolvableApiException: ResolvableApiException) :
    MarkersSingleEvent

  data object ZoomToCurrentLocation : MarkersSingleEvent
}

class MarkersViewModel(
  private val storesRepository: StoreRepository,
  private val androidLocationManager: AndroidLocationManager,
) : ViewModel() {
  private val _permissionStatusChannel = Channel<PermissionStatus>(
    capacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  )
  private val _gpsSettingsResultChannel = Channel<Boolean>(
    capacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  )
  private val eventChannel = Channel<MarkersSingleEvent>(Channel.UNLIMITED)
  private val _uiStateFlow = MutableStateFlow<MarkersUiState>(MarkersUiState.Uninitialized)

  val uiStateFlow: StateFlow<MarkersUiState> = _uiStateFlow.asStateFlow()
  val singleEventFlow: Flow<MarkersSingleEvent> = eventChannel.receiveAsFlow()

  internal fun onPermissionResult(permissionStatus: PermissionStatus) {
    Timber.d("onPermissionResult: permissionStatus=$permissionStatus")
    viewModelScope.launch { _permissionStatusChannel.send(permissionStatus) }
  }

  internal fun onGpsSettingsResult(enabled: Boolean) {
    Timber.d("onGpsSettingsResult: enabled=$enabled")
    viewModelScope.launch { _gpsSettingsResultChannel.send(enabled) }
  }

  internal fun getCurrentLocationAndStores() {
    viewModelScope.launch {
      when (uiStateFlow.value) {
        is MarkersUiState.Content, MarkersUiState.Loading -> {
          // do nothing
          return@launch
        }

        MarkersUiState.Error -> {
          // retry
        }

        MarkersUiState.Uninitialized -> {
          // the first time
        }
      }

      _uiStateFlow.value = MarkersUiState.Loading
      _uiStateFlow.value = getCurrentLocationAndStoresInternal()
    }
  }

  internal fun zoomToCurrentLocation() {
    viewModelScope.launch {
      eventChannel.trySend(MarkersSingleEvent.ZoomToCurrentLocation)

      _uiStateFlow.update { state ->
        when (state) {
          is MarkersUiState.Loading -> {
            // do nothing
            return@launch
          }

          MarkersUiState.Error, MarkersUiState.Uninitialized -> {
            // retry or the first time
            MarkersUiState.Loading
          }

          is MarkersUiState.Content -> {
            if (state.isRefreshing) {
              // do nothing
              return@launch
            } else {
              state.copy(isRefreshing = true)
            }
          }
        }
      }

      getCurrentLocationAndStoresInternal()
        .takeIf { it is MarkersUiState.Content }
        ?.let { _uiStateFlow.value = it }
    }
  }

  //region Internal
  @SuppressLint("MissingPermission")
  @CheckResult
  private suspend fun getCurrentLocationAndStoresInternal(): MarkersUiState {
    // await permission status from View
    eventChannel.trySend(MarkersSingleEvent.CheckLocationPermission)

    // handle permission status
    val permissionStatus = _permissionStatusChannel
      .receive()
      .also { Timber.d("getCurrentLocationAndStores: permission status=$it") }

    // get current location
    val currentLocationResult = when (permissionStatus) {
      PermissionStatus.Granted -> {
        if (isGpsSettingsEnabled()) {
          androidLocationManager.getCurrentLocation().getOrNull()
        } else {
          null
        }
      }

      is PermissionStatus.Denied,
      PermissionStatus.Undefined -> null
    }

    // get stores
    return if (currentLocationResult == null) {
      MarkersUiState.Error
    } else {
      storesRepository
        .getStores(currentLocationResult)
        .fold(
          onSuccess = { stores ->
            MarkersUiState.Content(
              currentLatLng = currentLocationResult.toUiModel(),
              zoomLevel = GoogleMapContentDefaults.ZoomLevel,
              stores = stores.map { it.toUiModel() }.toImmutableList(),
              isRefreshing = false,
            )
          },
          onFailure = { MarkersUiState.Error },
        )
    }
  }

  private suspend fun isGpsSettingsEnabled(): Boolean =
    androidLocationManager
      .checkLocationSettings()
      .fold(
        onSuccess = { true },
        onFailure = { error ->
          when (error) {
            is LocationSettingsError.ApiException -> false
            is LocationSettingsError.LocationSettingsDisabled -> {
              coroutineScope {
                // enable GPS settings
                eventChannel.trySend(MarkersSingleEvent.LocationSettingsDisabled(error.resolvableApiException))
                // await GPS status
                _gpsSettingsResultChannel.receive()
              }
            }

            else -> false
          }
        }
      )
  //endregion

  companion object {
    val factory = viewModelFactory {
      addInitializer(MarkersViewModel::class) {
        MarkersViewModel(
          storesRepository = MapsComposeAppLocator.storeRepository,
          androidLocationManager = MapsComposeAppLocator.androidLocationManager,
        )
      }
    }
  }
}
