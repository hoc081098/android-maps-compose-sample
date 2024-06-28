package com.hoc081098.mapscompose.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PermissionStatus {
  data object Granted : PermissionStatus
  data class Denied(val shouldShowRationale: Boolean) : PermissionStatus
  data object Undefined : PermissionStatus
}
