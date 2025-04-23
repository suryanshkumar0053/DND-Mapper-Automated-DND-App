package com.example.dndapp

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*


class LocationTrackingService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationUtils: LocationUtils

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking_channel"


        // 🔹 Start Service
        fun startService(context: Context) {
            val serviceIntent = Intent(context, LocationTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }

        // 🔹 Stop Service
        fun stopService(context: Context) {
            val serviceIntent = Intent(context, LocationTrackingService::class.java)
            context.stopService(serviceIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d("LocationService", "Service created")
        createNotificationChannel() // ✅ Ensure notification channel is created

        // Get the geofences flow from the provider
        val geofencesFlow = GeofenceFlowProvider.getFlow()

        // Pass the flow to LocationUtils to start observing it
        locationUtils = LocationUtils(
            context = this,
            dndUtils = DndUtils(), // Your DND utility
            locationRepository = locationRepository, // Your location repository
            geofencesFlow = geofencesFlow // Pass the flow to LocationUtils
        )
        Log.d("LocationService", "✅ LocationUtils initialized successfully.")
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification()) // ✅ Ensures notification is visible
        startLocationUpdates()
        Log.d("LocationService", "onStartCommand called - Starting foreground service")
        return START_STICKY // ✅ Keeps service running even if killed
    }


    private val locationRepository = LocationRepository() // Use the repository for live location updates

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 30_000L
        ).setMinUpdateIntervalMillis(5_000L).build()

        Log.d("LocationService", "Starting location updates")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d("LocationService", "New location: ${location.latitude}, ${location.longitude}")
                    locationRepository.updateLocation(location)
                }
            }
        }


        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Tracking your location in the background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true) // Prevents swipe dismissal
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // ✅ Visible on lock screen
            .setContentIntent(pendingIntent) // ✅ Opens the app when tapped
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Location Tracking", NotificationManager.IMPORTANCE_HIGH // ✅ Change to HIGH
            ).apply {
                enableLights(true)
                 lightColor = Color(0xFF0000FF).toArgb()
                enableVibration(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        Log.d("LocationService", "Tracking stopped and service destroyed.")

        GeofenceFlowProvider.clear() // Clear the flow reference when service is destroyed
    }

}
