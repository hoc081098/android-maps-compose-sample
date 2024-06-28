package com.hoc081098.mapscompose.presentation.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmapOrNull
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@ReadOnlyComposable
@Composable
fun bitmapDescriptorFactory(
  @DrawableRes resId: Int,
  @Px width: Int?,
  @Px height: Int?,
): BitmapDescriptor? =
  LocalContext.current.bitmapDescriptorFactory(
    resId = resId,
    width = width,
    height = height
  )

fun Context.bitmapDescriptorFactory(
  @DrawableRes resId: Int,
  @Px width: Int?,
  @Px height: Int?,
): BitmapDescriptor? {
  val drawable = AppCompatResources.getDrawable(this, resId)
  val bitmap = drawable
    ?.toBitmapOrNull(
      width = width ?: drawable.intrinsicWidth,
      height = height ?: drawable.intrinsicHeight,
    )
    ?: return null

  return BitmapDescriptorFactory.fromBitmap(bitmap)
}
