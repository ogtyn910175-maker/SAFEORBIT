package com.ogken.safeorbit.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ogken.safeorbit.MainActivity
import com.ogken.safeorbit.model.UserLocation
import com.ogken.safeorbit.repository.InMemoryLocationSharingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Foreground Service for voluntary, consent-based real-time location sharing.
 *
 * Privacy & Security Guarantees:
 * - Started ONLY when the user explicitly taps "Start Sharing" with verified consent.
 * - Displays an ongoing, non-dismissible notification clearly informing the user that location is shared.
 * - Provides a prominent "Stop Sharing" button directly in the notification.
 * - Stops location harvesting immediately upon stopping and clears/expires previous live coordinates in Firestore.
 * - Never starts automatically on boot or app launch.
 */
class LocationSharingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var currentCircleId: String = ""
    private var currentCircleName: String = "Family Circle"
    private var isPreciseLocation: Boolean = false

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val lastLocation = result.lastLocation ?: return
                publishLocationToFirestore(lastLocation)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_SHARING

        when (action) {
            ACTION_STOP_SHARING -> {
                stopLocationSharing()
                return START_NOT_STICKY
            }
            ACTION_START_SHARING -> {
                val circleId = intent?.getStringExtra(EXTRA_CIRCLE_ID).orEmpty()
                val circleName = intent?.getStringExtra(EXTRA_CIRCLE_NAME).orEmpty()
                currentCircleId = circleId
                currentCircleName = circleName.ifBlank { "Family Circle" }

                val fineGranted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                isPreciseLocation = fineGranted

                // Start Foreground with ongoing notification
                val notification = buildOngoingNotification(currentCircleName)
                startForeground(NOTIFICATION_ID, notification)

                // Sync repository state
                InMemoryLocationSharingRepository.instance.startSharingWithConsent(consentAcknowledged = true)

                // Update member card to active sharing in Firestore
                setMemberSharingActiveInFirestore(true)

                // Start location updates
                startLocationUpdates()
            }
        }

        return START_NOT_STICKY
    }

    private fun startLocationUpdates() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            stopLocationSharing()
            return
        }

        // Balanced intervals to preserve battery and data consumption
        val priority = if (fineGranted) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.Builder(priority, UPDATE_INTERVAL_MS).apply {
            setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            stopLocationSharing()
        }
    }

    private fun publishLocationToFirestore(location: Location) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        if (currentCircleId.isBlank()) return

        val batteryPercent = getBatteryPercentage()
        val userLocation = UserLocation(
            userId = currentUser.uid,
            circleId = currentCircleId,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            isLive = true,
            isApproximate = !isPreciseLocation,
            timestamp = System.currentTimeMillis(),
            batteryPercent = batteryPercent
        )

        serviceScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                // Publish to /circles/{circleId}/locations/{userId}
                db.collection("circles").document(currentCircleId)
                    .collection("locations").document(currentUser.uid)
                    .set(userLocation.toMap()).await()

                // Update battery and sharing active on member display card
                db.collection("circles").document(currentCircleId)
                    .collection("members").document(currentUser.uid)
                    .update(
                        mapOf(
                            "isSharingActive" to true,
                            "batteryPercent" to batteryPercent,
                            "lastSeenText" to "Just now",
                            "statusText" to if (isPreciseLocation) "Live (Precise)" else "Live (Approximate)"
                        )
                    ).await()
            } catch (_: Exception) {
                // Handled gracefully without crash
            }
        }
    }

    private fun stopLocationSharing() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}

        // Expire / remove previous location and update status in Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        val circleId = currentCircleId

        if (currentUser != null && circleId.isNotBlank()) {
            serviceScope.launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    // Remove or mark location not live so old coordinates are not shown as active
                    db.collection("circles").document(circleId)
                        .collection("locations").document(currentUser.uid)
                        .update(
                            mapOf(
                                "isLive" to false,
                                "timestamp" to System.currentTimeMillis()
                            )
                        ).await()

                    // Update member card
                    db.collection("circles").document(circleId)
                        .collection("members").document(currentUser.uid)
                        .update(
                            mapOf(
                                "isSharingActive" to false,
                                "statusText" to "Location Sharing OFF"
                            )
                        ).await()
                } catch (_: Exception) {}
            }
        }

        // Sync local repository state
        InMemoryLocationSharingRepository.instance.stopSharing()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setMemberSharingActiveInFirestore(active: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        if (currentCircleId.isBlank()) return

        serviceScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("circles").document(currentCircleId)
                    .collection("members").document(currentUser.uid)
                    .update("isSharingActive", active).await()
            } catch (_: Exception) {}
        }
    }

    private fun buildOngoingNotification(circleName: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LocationSharingService::class.java).apply {
            action = ACTION_STOP_SHARING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeOrbit Location Sharing Active")
            .setContentText("Sharing your live location with $circleName")
            .setSubText("Consent Active")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_delete,
                "Stop Sharing",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Sharing Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification displayed while SafeOrbit location sharing is active."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun getBatteryPercentage(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        return level.coerceIn(0, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "safeorbit_location_channel"
        const val NOTIFICATION_ID = 40401

        const val ACTION_START_SHARING = "com.ogken.safeorbit.action.START_LOCATION_SHARING"
        const val ACTION_STOP_SHARING = "com.ogken.safeorbit.action.STOP_LOCATION_SHARING"

        const val EXTRA_CIRCLE_ID = "extra_circle_id"
        const val EXTRA_CIRCLE_NAME = "extra_circle_name"

        // Sensible battery-efficient intervals: 15s interval, 10s fastest, 10m minimum distance
        private const val UPDATE_INTERVAL_MS = 15000L
        private const val FASTEST_INTERVAL_MS = 10000L
        private const val MIN_DISTANCE_METERS = 10.0f

        fun start(context: Context, circleId: String, circleName: String) {
            val intent = Intent(context, LocationSharingService::class.java).apply {
                action = ACTION_START_SHARING
                putExtra(EXTRA_CIRCLE_ID, circleId)
                putExtra(EXTRA_CIRCLE_NAME, circleName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationSharingService::class.java).apply {
                action = ACTION_STOP_SHARING
            }
            context.startService(intent)
        }
    }
}
