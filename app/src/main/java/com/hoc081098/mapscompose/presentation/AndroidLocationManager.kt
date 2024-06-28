package com.hoc081098.mapscompose.presentation

import com.google.android.gms.common.api.ApiException as GoogleApiException
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.CancellationTokenSource
import com.hoc081098.mapscompose.domain.DomainLatLng
import com.hoc081098.mapscompose.presentation.AndroidLocationManager.LocationError
import com.hoc081098.mapscompose.presentation.AndroidLocationManager.LocationSettingsError
import com.hoc081098.mapscompose.utils.mapFailure
import com.hoc081098.mapscompose.utils.retrySuspend
import com.hoc081098.mapscompose.utils.runSuspendCatching
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

interface AndroidLocationManager {
  suspend fun checkLocationSettings(): Result<Unit>

  sealed class LocationSettingsError(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    data class LocationSettingsDisabled(val resolvableApiException: ResolvableApiException) :
      LocationSettingsError(message = "Location settings is disable. Try to resolve!", cause = null)

    data class ApiException(val apiException: GoogleApiException) :
      LocationSettingsError(message = "Api exception: ${apiException.message}", cause = apiException)
  }

  sealed class LocationError(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    class LocationAvailabilityError : LocationError(message = "The location is unavailable", cause = null)

    class Unknown(cause: Throwable) : LocationError(message = cause.message, cause = cause)
  }

  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  suspend fun getCurrentLocation(): Result<DomainLatLng>
}

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

  override suspend fun checkLocationSettings() = withContext(Dispatchers.IO) {
    try {
      settingsClient.checkLocationSettings(locationSettingsRequest).await()
      Result.success(Unit)
    } catch (e: ResolvableApiException) {
      Timber.d(e, "Location settings is disable. Try to resolve!")

      Result.failure(LocationSettingsError.LocationSettingsDisabled(e))
    } catch (e: GoogleApiException) {
      Timber.e(e, "Failed to checkLocationSettings")

      Result.failure(LocationSettingsError.ApiException(e))
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  override suspend fun getCurrentLocation() = runSuspendCatching {
    withContext(Dispatchers.IO) {
      retrySuspend(
        times = 2,
        initialDelay = 300.milliseconds,
        factor = 2.0,
      ) {
        Timber.d("getCurrentLocation times=$it")

        val cancellationTokenSource = CancellationTokenSource()

        (
          fusedLocationClient
            .getCurrentLocation(
              currentLocationRequest,
              cancellationTokenSource.token
            )
            .await(cancellationTokenSource)
            ?: throw LocationError.LocationAvailabilityError()
          )
          .toLatLng()
      }
    }
  }
    .mapFailure { (it as? LocationError) ?: LocationError.Unknown(it) }
    .onSuccess { Timber.d("getCurrentLocation: $it")}
    .onFailure { Timber.e(it, "Cannot get current location") }

  @Suppress("unused")
  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  private suspend fun getCurrentLocationInternal(): Location =
    suspendCancellableCoroutine { continuation ->
      Timber.d("[getCurrentLocation]")

      val locationCallback = AtomicReference<LocationCallback?>()

      fun removeCallback() {
        locationCallback.getAndSet(null)?.let { callback ->
          fusedLocationClient.removeLocationUpdates(callback).addOnFailureListener {
            // ignore this exception
            Timber.e(it, "[removeCallback]")
          }
          Timber.d("[removeCallback]")
        }
      }

      locationCallback.set(
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            (locationResult.lastLocation ?: return).let { location ->
              continuation
                .takeIf { it.isActive }
                ?.run {
                  Timber.d("getCurrentLocation: [locationCallback] resume: $location")

                  removeCallback()
                  resume(location)
                }
            }
          }

          override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if (!locationAvailability.isLocationAvailable) {
              continuation
                .takeIf { it.isActive }
                ?.run {
                  Timber.d("getCurrentLocation: [locationCallback] onLocationAvailability resumeException")

                  removeCallback()
                  resumeWithException(LocationError.LocationAvailabilityError())
                }
            }
          }
        }
      )

      continuation.invokeOnCancellation {
        removeCallback()
        Timber.d("[invokeOnCancellation]")
      }

      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback.get()!!,
        Looper.getMainLooper(),
      ).run {
        val resumeWithUnknownException: (Exception) -> Unit = { exception ->
          continuation
            .takeIf { it.isActive }
            ?.run {
              Timber.e(exception, "getCurrentLocation: [locationCallback] requestLocationUpdates resumeException")
              resumeWithException(LocationError.Unknown(exception))
            }
        }

        if (isComplete) {
          exception?.let(resumeWithUnknownException)
        } else {
          addOnFailureListener(resumeWithUnknownException)
        }
      }
    }

  private companion object {
    private val LOCATION_REQUEST_INTERVAL = 1.seconds
    private val LOCATION_REQUEST_FASTEST_INTERVAL = 500.milliseconds
    private val LOCATION_REQUEST_MAX_WAIT_TIME = 3.seconds

    private val CURRENT_LOCATION_DURATION = 5.seconds
    private val CURRENT_LOCATION_MAX_UPDATE_AGE = 5.minutes
  }
}

@VisibleForTesting
internal fun Location.toLatLng() = DomainLatLng(
  latitude = latitude,
  longitude = longitude
)
