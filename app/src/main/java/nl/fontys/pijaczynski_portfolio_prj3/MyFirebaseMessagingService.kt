package nl.fontys.pijaczynski_portfolio_prj3

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Called when Firebase gives this device a (new) address = token.
    // A backend needs this token to send a message to exactly this device.
    override fun onNewToken(token: String) {
        Log.d(TAG, "New token: $token")
    }

    // Called when a message arrives while the app is OPEN (foreground).
    // When the app is in the background, Android shows the notification itself.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification: ${it.title} - ${it.body}")
            showNotification(it.title ?: "New message", it.body ?: "")
        }
    }

    // Show the message ourselves, so it is also visible while the app is open
    @SuppressLint("MissingPermission") // checked at the top of the function
    private fun showNotification(title: String, body: String) {
        // Android 13+: without POST_NOTIFICATIONS nothing is shown
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        // Android 8+: every notification needs a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "General", NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "general"
    }
}
