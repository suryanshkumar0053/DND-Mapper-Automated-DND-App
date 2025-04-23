package com.example.dndapp


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient

object LocationPermissionHelper {

    // Function to check if Foreground (Fine & Coarse) and Background permissions are granted
   /* fun hasLocationPermissions(context: Context): Boolean {
        val foregroundGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // ✅ Background location permission is not required for API < 29
        }


        return foregroundGranted && backgroundGranted
    }*/

    // Function to check if only Foreground permissions are granted
    fun hasForegroundLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to check if Background permission is granted
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // ✅ Background location permission is not required for API < 29
        }

    }

    fun hasPostNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required below API 33
        }
    }

    // Function to open App Settings for Background Permission
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun isLocationAccuracyEnabled(context: Context): Boolean {
        return try {
            val locationMode = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            )
            locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY
        } catch (e: Exception) {
            false
        }
    }

    fun openLocationAccuracySettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun requestHighAccuracy(context: Context, onResult: (Boolean) -> Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)  // This ensures the dialog appears like Google Maps

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onResult(true)  // High Accuracy mode is enabled
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(context as Activity, 1010)
                } catch (sendEx: IntentSender.SendIntentException) {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        }
    }

    fun promptEnableHighAccuracy(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val locationRequest = LocationRequest.create()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)

        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                // Already enabled
                Log.d("LocationHelper", "High accuracy already enabled")
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(e.resolution).build()
                        launcher.launch(intentSenderRequest)
                    } catch (sendEx: Exception) {
                        Log.e("LocationHelper", "Error launching accuracy dialog", sendEx)
                    }
                }
            }
    }


}