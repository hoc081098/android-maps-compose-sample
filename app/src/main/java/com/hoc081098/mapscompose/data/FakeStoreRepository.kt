package com.hoc081098.mapscompose.data

import com.hoc081098.mapscompose.domain.DomainLatLng
import com.hoc081098.mapscompose.domain.Store
import com.hoc081098.mapscompose.domain.StoreRepository
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

class FakeStoreRepository : StoreRepository {
  override suspend fun getStores(center: DomainLatLng): Result<List<Store>> {
    delay(500)
    return Result.success(
      (500..20_000 step 250)
        .flatMap { genLatLngsInCircle(center, it.toDouble()) }
        .shuffled()
        .take(200)
        .mapIndexed { index, domainLatLng ->
          Store(
            id = index.toString(),
            name = "Store #$index",
            address = "Address of #$index",
            latLng = domainLatLng,
            description = "Description of #$index",
            isFavorite = index % 3 == 0,
          )
        }
    )
  }
}

private fun genLatLngsInCircle(center: DomainLatLng, radiusInMeter: Double): List<DomainLatLng> {
  val earthRadiusMeters = 6371000.0 // Earth's radius in meters (approximate)
  val numPoints = 64 // Number of points to define the circle (more points = smoother circle)
  val dTheta = 2.0 * PI / numPoints // Angle between each point (in radians)
  val points = mutableListOf<DomainLatLng>()
  val latRad = Math.toRadians(center.latitude)
  val lngRad = Math.toRadians(center.longitude)

  for (i in 0 until numPoints) {
    val theta = i * dTheta
    val distanceRatio = radiusInMeter / earthRadiusMeters
    val newLat =
      asin(sin(latRad) * cos(distanceRatio) + cos(latRad) * sin(distanceRatio) * cos(theta))
    val newLng =
      lngRad + atan2(
        sin(theta) * sin(distanceRatio) * cos(latRad),
        cos(distanceRatio) - sin(latRad) * sin(newLat)
      )
    points.add(DomainLatLng(Math.toDegrees(newLat), Math.toDegrees(newLng)))
  }

  return points
}
