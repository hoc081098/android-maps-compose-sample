package com.hoc081098.mapscompose.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Context.isPermissionGranted(permission: String): Boolean {
  return ContextCompat.checkSelfPermission(this, permission) ==
    PackageManager.PERMISSION_GRANTED
}

fun Activity.shouldShowRationale(permission: String): Boolean {
  return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}
