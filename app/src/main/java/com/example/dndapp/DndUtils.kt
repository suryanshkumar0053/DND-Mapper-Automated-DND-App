package com.example.dndapp


import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log

open class DndUtils () {



    fun isDNDActive(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android M (API level 23) and above, use NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
        } else {
            // For devices below Android M, check the ringer mode
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        }
    }





    /**
     * Checks if the app has DND access permission.
     *
     * @param context The application context.
     * @return `true` if permission is granted, otherwise `false`.
     */
    fun hasDNDPermission(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Enables DND mode if the app has permission.
     *
     * @param context The application context.
     *
     */




    fun enableDND(context: Context) {
        if (hasDNDPermission(context)) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            Log.d("DNDUtils", "DND Mode Enabled")
        } else {
            Log.e("DNDUtils", "DND Permission Not Granted")
            openDNDSettings(context) // Open settings for the user to grant permission
        }
    }

    /**
     * Disables DND mode if the app has permission.
     *
     * @param context The application context.
     */
    fun disableDND(context: Context) {
        if (hasDNDPermission(context)) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            Log.d("DNDUtils", "DND Mode Disabled")
        } else {
            Log.e("DNDUtils", "DND Permission Not Granted")
        }
    }

    /**
     * Opens the system DND settings page for the user to grant DND access.
     *
     * @param context The application context.
     */
    fun openDNDSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required to start settings page from app
        context.startActivity(intent)
    }
}
