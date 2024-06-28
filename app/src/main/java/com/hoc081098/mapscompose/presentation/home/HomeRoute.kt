package com.hoc081098.mapscompose.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoc081098.mapscompose.ui.theme.MapsComposeTheme
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onNavigateToBasicMarkers: () -> Unit,
  onNavigateToAdvancedMarkers: () -> Unit,
  onNavigateToClusteredMarkers: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(text = "Maps Compose")
        }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        ElevatedButton(onClick = onNavigateToBasicMarkers) {
          Text(text = "Basic markers")
        }

        ElevatedButton(onClick = onNavigateToAdvancedMarkers) {
          Text(text = "Advanced markers")
        }

        ElevatedButton(onClick = onNavigateToClusteredMarkers) {
          Text(text = "Clustered markers")
        }
      }
    }
  }
}

@Preview
@Composable
private fun HomeScreenPreview() {
  MapsComposeTheme {
    HomeScreen(
      onNavigateToBasicMarkers = {},
      onNavigateToAdvancedMarkers = {},
      onNavigateToClusteredMarkers = {},
    )
  }
}
