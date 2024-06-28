package com.hoc081098.mapscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hoc081098.mapscompose.presentation.basic.BasicMarkersRoute
import com.hoc081098.mapscompose.presentation.basic.BasicMarkersScreen
import com.hoc081098.mapscompose.presentation.home.HomeRoute
import com.hoc081098.mapscompose.presentation.home.HomeScreen
import com.hoc081098.mapscompose.ui.theme.MapsComposeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MapsComposeTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = HomeRoute) {
          composable<HomeRoute> {
            HomeScreen(
              onNavigateToBasicMarkers = dropUnlessResumed {
                navController.navigate(route = BasicMarkersRoute)
              },
              onNavigateToAdvancedMarkers = {},
              onNavigateToClusteredMarkers = {},
            )
          }

          composable<BasicMarkersRoute> {
            BasicMarkersScreen()
          }
        }
      }
    }
  }
}

