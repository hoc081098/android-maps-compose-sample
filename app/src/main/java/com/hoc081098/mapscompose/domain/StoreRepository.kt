package com.hoc081098.mapscompose.domain

interface StoreRepository {
  suspend fun getStores(center: DomainLatLng): Result<List<Store>>
}
