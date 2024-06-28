package com.hoc081098.mapscompose.presentation.models

import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.LatLng
import com.hoc081098.mapscompose.domain.DomainLatLng

@Immutable
data class LatLngUiModel(
  val latitude: Double,
  val longitude: Double
)

fun DomainLatLng.toUiModel() = LatLngUiModel(latitude = latitude, longitude = longitude)

fun LatLngUiModel.toGmsLatLng(): LatLng = LatLng(/* latitude = */ latitude, /* longitude = */ longitude)
