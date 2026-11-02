package nl.fontys.pijaczynski_portfolio_prj3

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import nl.fontys.pijaczynski_portfolio_prj3.ui.theme.Pijaczynski_Portfolio_PRJ3Theme
import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.os.Build
import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pijaczynski_Portfolio_PRJ3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var screen by remember { mutableStateOf("home") }
                    when (screen) {
                        "camera" -> CameraScreen(onBack = { screen = "home" }, modifier = Modifier.padding(innerPadding))
                        "map" -> MapScreen(onBack = { screen = "home" }, modifier = Modifier.padding(innerPadding))
                        "places" -> PlacesScreen(onBack = { screen = "home" }, modifier = Modifier.padding(innerPadding))
                        else -> PermissionScreen(
                            onOpenCamera = { screen = "camera" },
                            onOpenMap = { screen = "map" },
                            onOpenPlaces = { screen = "places" },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
@SuppressLint("MissingPermission")
@Composable
fun PermissionScreen(modifier: Modifier = Modifier, onOpenCamera: () -> Unit, onOpenMap: () -> Unit, onOpenPlaces: () -> Unit) {
    val context = LocalContext.current

    // 1. Check: do we already have it?
    fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

    var cameraGranted by remember { mutableStateOf(isGranted(Manifest.permission.CAMERA)) }
    var locationGranted by remember {
        mutableStateOf(isGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    // Android 13+ asks for notifications at runtime, older versions allow them by default
    fun notificationsAllowed() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                isGranted(Manifest.permission.POST_NOTIFICATIONS)
    var notificationsGranted by remember { mutableStateOf(notificationsAllowed()) }

    // 2. Launcher: shows the system pop-up, the result comes back here
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        cameraGranted = isGranted(Manifest.permission.CAMERA)
        locationGranted = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        notificationsGranted = notificationsAllowed()
    }

    // Push notifications: get this device's token and subscribe to a topic
    var pushStatus by remember { mutableStateOf("Connecting to Firebase...") }
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM", "Token: $token")   // copy it from Logcat (filter: FCM)
        }
        FirebaseMessaging.getInstance().subscribeToTopic("portfolio")
            .addOnCompleteListener { task ->
                pushStatus = if (task.isSuccessful) "Subscribed to topic \"portfolio\""
                else "Topic subscription failed"
            }
    }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationText by remember { mutableStateOf("No location yet") }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Camera: ${if (cameraGranted) "granted" else "not granted"}")
        Text("Location: ${if (locationGranted) "granted" else "not granted"}")

        // 3. Ask only for what's missing
        Button(onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA)) }) {
            Text("Allow camera")
        }
        Button(onClick = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }) { Text("Allow location") }

        Button(
            enabled = locationGranted,
            onClick = {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        locationText = if (loc != null) "Lat ${loc.latitude}, Lng ${loc.longitude}"
                        else "Location unavailable"

                    }
            }
        ) { Text("Get my location") }
        Text(locationText)
        Button(enabled = cameraGranted, onClick = onOpenCamera) { Text("Open camera") }
        Button(enabled = locationGranted, onClick = onOpenMap) { Text("Open map") }
        Button(onClick = onOpenPlaces) { Text("Saved places (backend)") }

        Text("Notifications: ${if (notificationsGranted) "granted" else "not granted"}")
        Button(
            enabled = !notificationsGranted,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
        ) { Text("Allow notifications") }
        Text(pushStatus)
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pijaczynski_Portfolio_PRJ3Theme {
        Greeting("Android")
    }


}