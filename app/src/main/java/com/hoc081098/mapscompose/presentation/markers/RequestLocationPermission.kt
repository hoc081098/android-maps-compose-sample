package com.hoc081098.mapscompose.presentation.markers

import android.Manifest
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.hoc081098.mapscompose.presentation.models.PermissionStatus
import com.hoc081098.mapscompose.presentation.utils.findActivity
import com.hoc081098.mapscompose.presentation.utils.isPermissionGranted
import com.hoc081098.mapscompose.presentation.utils.shouldShowRationale
import timber.log.Timber

private val LocationPermissions = listOf(
  Manifest.permission.ACCESS_COARSE_LOCATION,
  Manifest.permission.ACCESS_FINE_LOCATION,
)

@Composable
fun rememberRequestMultipleLocationPermissionsEffect(
  vararg keys: Any?,
  onPermissionsResult: (PermissionStatus) -> Unit,
): () -> Unit {
  val context = LocalContext.current
  val currentOnPermissionsResult by rememberUpdatedState(onPermissionsResult)

  val locationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { result ->
    Timber.d("requestPermissionLauncher: [onPermissionsResult] result=$result")

    val permissionStatus = if (result.values.any { it }) {
      PermissionStatus.Granted
    } else {
      PermissionStatus.Denied(
        shouldShowRationale = LocationPermissions
          .any { context.findActivity().shouldShowRationale(it) }
      )
    }.also {
      Timber.d("requestPermissionLauncher: [onPermissionsResult] permissionStatus=$it")
    }
    onPermissionsResult(permissionStatus)
  }

  return remember(keys, context, locationPermissionLauncher) {
    {
      context.checkLocationPermission(
        onPermissionsResult = currentOnPermissionsResult,
        locationPermissionLauncher = locationPermissionLauncher
      )
    }
  }
}

private fun Context.checkLocationPermission(
  onPermissionsResult: (PermissionStatus) -> Unit,
  locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
  when {
    LocationPermissions.any { isPermissionGranted(it) } -> {
      Timber.d("requestPermissionLauncher: [granted]")
      onPermissionsResult(PermissionStatus.Granted)
    }

    LocationPermissions.any { findActivity().shouldShowRationale(it) } -> {
      Timber.d("requestPermissionLauncher: [shouldShowRationale]")
      onPermissionsResult(PermissionStatus.Denied(shouldShowRationale = true))
    }

    else -> {
      Timber.d("requestPermissionLauncher: [launch]")
      locationPermissionLauncher.launch(LocationPermissions.toTypedArray())
    }
  }
}
