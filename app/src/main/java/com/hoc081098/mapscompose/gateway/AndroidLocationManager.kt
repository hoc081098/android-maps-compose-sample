package com.hoc081098.mapscompose.gateway

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.api.ResolvableApiException
import com.hoc081098.mapscompose.domain.DomainLatLng

typealias GoogleApiException = com.google.android.gms.common.api.ApiException

interface AndroidLocationManager {
  suspend fun checkLocationSettings(): Result<Unit>

  @RequiresPermission(anyOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
  suspend fun getCurrentLocation(): Result<DomainLatLng>

  sealed class LocationSettingsError(message: String?, cause: Throwable?) :
    RuntimeException(message, cause) {
    data class LocationSettingsDisabled(val resolvableApiException: ResolvableApiException) :
      LocationSettingsError(message = "Location settings is disable. Try to resolve!", cause = null)

    data class ApiException(val apiException: GoogleApiException) :
      LocationSettingsError(
        message = "Api exception: ${apiException.message}",
        cause = apiException
      )
  }

  sealed class LocationError(message: String?, cause: Throwable?) :
    RuntimeException(message, cause) {
    class LocationAvailabilityError :
      LocationError(message = "The location is unavailable", cause = null)

    class Unknown(cause: Throwable) : LocationError(message = cause.message, cause = cause)
  }
}
