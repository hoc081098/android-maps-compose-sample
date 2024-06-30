package com.hoc081098.mapscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoc081098.mapscompose.presentation.advanced.AdvancedMarkersScreen
import com.hoc081098.mapscompose.presentation.basic.BasicMarkersScreen
import com.hoc081098.mapscompose.presentation.clustered.ClusteredMarkersScreen
import com.hoc081098.mapscompose.presentation.markers.MarkersScreen
import com.hoc081098.mapscompose.presentation.markers.MarkersViewModel
import com.hoc081098.mapscompose.ui.theme.MapsComposeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val viewModel = viewModel<MarkersViewModel>(factory = MarkersViewModel.factory)

      // Remember the type of marker we want to show
      var selectedMarkerType by rememberSaveable { mutableStateOf(MarkerType.Basic) }

      MapsComposeTheme(dynamicColor = false) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            MapsComposeTopBar(
              topBarTitleStringRes = selectedMarkerType.title,
              showAllChecked = false,
              onAnimateToCurrentLocation = viewModel::zoomToCurrentLocation,
              onToggleShowAllClick = {},
            )
          },
          bottomBar = {
            BottomNav(
              selectedScreen = selectedMarkerType,
              onMarkerTypeClicked = { selectedMarkerType = it },
            )
          },
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.Center,
          ) {
            MarkersScreen(
              viewModel = viewModel,
              onDrag = { },
            ) { uiState ->
              when (val t = selectedMarkerType) {
                MarkerType.Basic ->
                  BasicMarkersScreen(uiState = uiState)

                MarkerType.Advanced ->
                  AdvancedMarkersScreen(uiState = uiState)

                MarkerType.Clustered ->
                  ClusteredMarkersScreen(uiState = uiState)
              }
            }
          }
        }
      }
    }
  }
}

