package com.hoc081098.mapscompose.presentation.models

import androidx.compose.runtime.Immutable
import com.hoc081098.mapscompose.domain.Store

@Immutable
data class StoreUiModel(
  val id: String,
  val name: String,
  val address: String,
  val latLng: LatLngUiModel,
  val description: String,
  val isFavorite: Boolean
)

fun Store.toUiModel() = StoreUiModel(
  id = id,
  name = name,
  address = address,
  latLng = latLng.toUiModel(),
  description = description,
  isFavorite = isFavorite
)
