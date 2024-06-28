package com.hoc081098.mapscompose.presentation.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

tailrec fun Context.findActivity(): Activity = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> error("Could not find activity in Context chain.")
}
