package com.hoc081098.mapscompose

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.hoc081098.mapscompose.data.FakeStoreRepository
import com.hoc081098.mapscompose.data.GmsAndroidLocationManager
import com.hoc081098.mapscompose.gateway.AndroidLocationManager
import timber.log.Timber

class MapsComposeApp : Application() {
  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { renderer ->
      when (renderer) {
        MapsInitializer.Renderer.LATEST ->
          Timber.tag("MapsDemo").d("The latest version of the renderer is used.")

        MapsInitializer.Renderer.LEGACY ->
          Timber.tag("MapsDemo").d("The legacy version of the renderer is used.")

        else -> Unit
      }
    }

    MapsComposeAppLocator.initialize(this)
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

  val storeRepository: FakeStoreRepository
    get() = FakeStoreRepository()
}
