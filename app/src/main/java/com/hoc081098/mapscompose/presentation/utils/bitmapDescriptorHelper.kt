package com.hoc081098.mapscompose.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.hoc081098.mapscompose.R

data class StoreBitmapDescriptors(
  val selectedNormalStoreIcon: BitmapDescriptor?,
  val selectedFavoriteStoreIcon: BitmapDescriptor?,
  val unselectedNormalStoreIcon: BitmapDescriptor?,
  val unselectedFavoriteStoreIcon: BitmapDescriptor?,
  val currentLocationIcon: BitmapDescriptor?,
)

private val NormalSize = 36.dp
private val SelectedSize = NormalSize * 1.5f

@Composable
fun rememberStoreBitmapDescriptors(): StoreBitmapDescriptors {
  return StoreBitmapDescriptors(
    selectedNormalStoreIcon = rememberStoreIconBitmapDescriptor(
      isFavorite = false,
      iconSize = SelectedSize,
    ),
    selectedFavoriteStoreIcon = rememberStoreIconBitmapDescriptor(
      isFavorite = true,
      iconSize = SelectedSize,
    ),
    unselectedNormalStoreIcon = rememberStoreIconBitmapDescriptor(
      isFavorite = false,
      iconSize = NormalSize,
    ),
    unselectedFavoriteStoreIcon = rememberStoreIconBitmapDescriptor(
      isFavorite = true,
      iconSize = NormalSize,
    ),
    currentLocationIcon = rememberCurrentLocationBitmapDescriptor(),
  )
}

@Composable
fun rememberStoreIconBitmapDescriptor(
  isFavorite: Boolean,
  iconSize: Dp
): BitmapDescriptor? {
  val context = LocalContext.current
  val density = LocalDensity.current

  return remember(context, density, iconSize) {
    val currentIconSizeInDp = density.run { iconSize.toPx() }.toInt()
    context.bitmapDescriptorFactory(
      resId = if (isFavorite) R.drawable.ic_store_96 else R.drawable.ic_store_100,
      width = currentIconSizeInDp,
      height = currentIconSizeInDp,
    )
  }
}

@Composable
fun rememberCurrentLocationBitmapDescriptor(): BitmapDescriptor? {
  val context = LocalContext.current
  val density = LocalDensity.current

  return remember(context, density) {
    val currentIconSizeInDp = density.run { 64.dp.toPx() }.toInt()

    context.bitmapDescriptorFactory(
      resId = R.drawable.ic_current_location_96,
      width = currentIconSizeInDp,
      height = currentIconSizeInDp,
    )
  }
}
