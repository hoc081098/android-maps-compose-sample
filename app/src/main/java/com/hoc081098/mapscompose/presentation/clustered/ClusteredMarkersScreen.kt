package com.hoc081098.mapscompose.presentation.clustered

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.hoc081098.mapscompose.R
import com.hoc081098.mapscompose.presentation.markers.MarkersUiState
import com.hoc081098.mapscompose.presentation.models.StoreUiModel
import com.hoc081098.mapscompose.presentation.models.toGmsLatLng
import com.hoc081098.mapscompose.ui.theme.MapsComposeTheme
import timber.log.Timber

private class StoreClusterItem(val store: StoreUiModel) : ClusterItem {
  override fun getPosition() = store.latLng.toGmsLatLng()
  override fun getTitle() = store.name
  override fun getSnippet() = store.description
  override fun getZIndex() = if (store.isFavorite) 5f else 2f
}

@Immutable
private data class StoreItemIconColors(
  val iconColor: Color,
  val backgroundColor: Color,
  val borderColor: Color
)


@OptIn(MapsComposeExperimentalApi::class)
@Composable
@GoogleMapComposable
fun ClusteredMarkersScreen(
  uiState: MarkersUiState.Content,
) {
  val items = remember(uiState.stores) { uiState.stores.map(::StoreClusterItem) }

  val backgroundAlpha = 0.8f
  val favoriteColors = StoreItemIconColors(
    iconColor = MaterialTheme.colorScheme.onPrimary,
    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha),
    borderColor = MaterialTheme.colorScheme.primary
  )
  val normalColors = StoreItemIconColors(
    iconColor = MaterialTheme.colorScheme.secondary,
    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = backgroundAlpha),
    borderColor = MaterialTheme.colorScheme.secondary
  )

  Clustering(
    items = items,
    onClusterClick = {
      Timber.d("onClusterClick: $it")
      false
    },
    onClusterItemClick = {
      Timber.d("onClusterItemClick: $it")
      false
    },
    clusterItemContent = {
      SingleStoreItem(
        colors = if (it.store.isFavorite) favoriteColors else normalColors,
      )
    },
  )
}

@Composable
@NonRestartableComposable
private fun SingleStoreItem(
  colors: StoreItemIconColors,
  modifier: Modifier = Modifier
) {
  Icon(
    modifier = modifier
      .size(32.dp)
      .padding(1.dp)
      .drawBehind {
        drawCircle(color = colors.backgroundColor, style = Fill)
        drawCircle(color = colors.borderColor, style = Stroke(width = 3f))
      }
      .padding(4.dp),
    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_store_24),
    tint = colors.iconColor,
    contentDescription = "",
  )
}

@Preview
@Composable
private fun SingleStoreItemPreview() {
  MapsComposeTheme {
    SingleStoreItem(
      StoreItemIconColors(
        iconColor = MaterialTheme.colorScheme.onBackground,
        backgroundColor = MaterialTheme.colorScheme.background,
        borderColor = MaterialTheme.colorScheme.onBackground,
      )
    )
  }
}
