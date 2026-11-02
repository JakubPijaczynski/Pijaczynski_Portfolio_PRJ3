package nl.fontys.pijaczynski_portfolio_prj3

import android.annotation.SuppressLint
import org.osmdroid.config.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@SuppressLint("MissingPermission")
@Composable
fun MapScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val mapView = remember {
        // OSM rule: every app must identify itself, otherwise you get no tiles
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)   // pinch to zoom
            controller.setZoom(16.0)
        }
    }
    val marker = remember { Marker(mapView).apply { title = "You are here" } }

    // Runs once when the screen opens: get location, move map + marker there
    LaunchedEffect(Unit) {
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    mapView.controller.setCenter(point)
                    marker.position = point
                    if (!mapView.overlays.contains(marker)) mapView.overlays.add(marker)
                    mapView.invalidate()   // redraw
                }
            }
    }
    // Clean up the map when you leave the screen
    DisposableEffect(Unit) { onDispose { mapView.onDetach() } }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxWidth().weight(1f))
        Button(onClick = onBack) { Text("Back") }
    }
}