plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.secrets.gradle.plugin)
}

android {
  namespace = "com.hoc081098.mapscompose"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.hoc081098.mapscompose"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.appcompat.resources)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
  implementation("com.jakewharton.timber:timber:5.0.1")

  // Google Maps Compose library
  implementation(libs.maps.compose)
  // Google Maps Compose utility library
  implementation(libs.maps.compose.utils)
  // Google Maps Compose widgets library
  implementation(libs.maps.compose.widgets)

  // Play Services Location
  implementation(libs.play.services.location)
  implementation("com.google.android.gms:play-services-maps:19.0.0")
}


secrets {
  // A properties file containing default secret values.
  // This file can be checked in version control.
  defaultPropertiesFileName = "secrets.defaults.properties"
}

composeCompiler {
  enableStrongSkippingMode = true
}
