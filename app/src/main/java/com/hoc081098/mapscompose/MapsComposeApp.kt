package com.hoc081098.mapscompose

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.hoc081098.mapscompose.data.FakeStoreRepository
import com.hoc081098.mapscompose.presentation.AndroidLocationManager
import com.hoc081098.mapscompose.presentation.GmsAndroidLocationManager
import timber.log.Timber

class MapsComposeApp : Application() {
  override fun onCreate() {
    super.onCreate()
    MapsInitializer.initialize(this)
    MapsComposeAppLocator.initialize(this)
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}

object MapsComposeAppLocator {
  private lateinit var app: MapsComposeApp

  fun initialize(app: MapsComposeApp) {
    this.app = app
  }

  private val fusedLocationClient by lazy {
    LocationServices.getFusedLocationProviderClient(app)
  }
  private val settingsClient by lazy {
    LocationServices.getSettingsClient(app)
  }

  val androidLocationManager: AndroidLocationManager by lazy {
    GmsAndroidLocationManager(
      fusedLocationClient = fusedLocationClient,
      settingsClient = settingsClient,
    )
  }
  val storeRepository by lazy {
    FakeStoreRepository()
  }
}
