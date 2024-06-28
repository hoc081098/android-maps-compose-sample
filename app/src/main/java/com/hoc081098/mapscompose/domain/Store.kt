package com.hoc081098.mapscompose.domain

data class Store(
  val id: String,
  val name: String,
  val address: String,
  val latLng: DomainLatLng,
  val description: String
)
