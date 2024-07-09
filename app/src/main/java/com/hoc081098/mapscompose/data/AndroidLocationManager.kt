package com.hoc081098.mapscompose.data

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import com.hoc081098.mapscompose.domain.DomainLatLng
import com.hoc081098.mapscompose.gateway.AndroidLocationManager
import com.hoc081098.mapscompose.gateway.AndroidLocationManager.LocationError
import com.hoc081098.mapscompose.gateway.AndroidLocationManager.LocationSettingsError
import com.hoc081098.mapscompose.gateway.GoogleApiException
import com.hoc081098.mapscompose.utils.mapFailure
import com.hoc081098.mapscompose.utils.retrySuspend
import com.hoc081098.mapscompose.utils.runSuspendCatching
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import timber.log.Timber

internal class GmsAndroidLocationManager(
  private val fusedLocationClient: FusedLocationProviderClient,
  private val settingsClient: SettingsClient,
) : AndroidLocationManager {
  private val locationSettingsRequest: LocationSettingsRequest by lazy {
    LocationSettingsRequest.Builder()
      .addLocationRequest(locationRequest)
      .setAlwaysShow(true)
      .build()
  }
  private val locationRequest: LocationRequest by lazy {
    LocationRequest
      .Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_REQUEST_INTERVAL.inWholeMilliseconds
      )
      .setMinUpdateIntervalMillis(LOCATION_REQUEST_FASTEST_INTERVAL.inWholeMilliseconds)
      .setMaxUpdateDelayMillis(LOCATION_REQUEST_MAX_WAIT_TIME.inWholeMilliseconds)
      .setMaxUpdates(1)
      .build()
  }
  private val currentLocationRequest by lazy {
    CurrentLocationRequest.Builder()
      .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
      .setDurationMillis(CURRENT_LOCATION_DURATION.inWholeMilliseconds)
      .setMaxUpdateAgeMillis(CURRENT_LOCATION_MAX_UPDATE_AGE.inWholeMilliseconds)
      .build()
  }

  override suspend fun checkLocationSettings() =
    runSuspendCatching(Dispatchers.IO) {
      settingsClient.checkLocationSettings(locationSettingsRequest).await()
      Unit
    }
      .mapFailure { e ->
        when (e) {
          is ResolvableApiException -> LocationSettingsError.LocationSettingsDisabled(e)
          is GoogleApiException -> LocationSettingsError.ApiException(e)
          else -> e
        }
      }
      .onSuccess { Timber.d("checkLocationSettings: successfully") }
      .onFailure { Timber.e(it, "checkLocationSettings failed") }

  @OptIn(ExperimentalCoroutinesApi::class)
  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  override suspend fun getCurrentLocation() =
    runSuspendCatching(Dispatchers.IO) {
      retrySuspend(
        times = 2,
        initialDelay = 300.milliseconds,
        factor = 2.0,
      ) {
        Timber.d("getCurrentLocation times=$it")

        val cancellationTokenSource = CancellationTokenSource()
        val location = fusedLocationClient
          .getCurrentLocation(
            currentLocationRequest,
            cancellationTokenSource.token
          )
          .await(cancellationTokenSource)
          ?: throw LocationError.LocationAvailabilityError()

        location.toDomainLatLng()
      }
    }
      .mapFailure { (it as? LocationError) ?: LocationError.Unknown(it) }
      .onSuccess { Timber.d("getCurrentLocation: $it") }
      .onFailure { Timber.e(it, "getCurrentLocation failed") }

  private companion object {
    private val LOCATION_REQUEST_INTERVAL = 1.seconds
    private val LOCATION_REQUEST_FASTEST_INTERVAL = 500.milliseconds
    private val LOCATION_REQUEST_MAX_WAIT_TIME = 3.seconds

    private val CURRENT_LOCATION_DURATION = 5.seconds
    private val CURRENT_LOCATION_MAX_UPDATE_AGE = 5.minutes
  }
}

@VisibleForTesting
internal fun Location.toDomainLatLng() = DomainLatLng(
  latitude = latitude,
  longitude = longitude
)
