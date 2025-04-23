# -DND-Mapper-Automated-DND-App-

📵 DND Location Tracker App
An Android app built with Kotlin and Jetpack Compose that automatically switches your phone to Do Not Disturb (DND) mode when you enter a user-defined location. It tracks your location using a foreground service, uses the Google Maps API for location selection, and saves predefined DND zones locally via Room database. It also uses Google Geocoding API to fetch human-readable location names.

🛠️ Tech Stack

Kotlin

Jetpack Compose (for UI)

MVVM Architecture

Room Database (local storage of geofences)

Google Maps SDK

Google Geocoding API

Foreground Service (for background location tracking)

DND Access APIs

StateFlow, LiveData, and ViewModel

Permissions API (Android 13+)

✨ Features

📍 Select predefined locations (geofences) using Google Map

🔕 Automatically switches to DND Mode when entering those locations

🗃 Stores geofence data locally (lat, lon, radius, name) via Room DB

🌐 Fetches location names using Google Geocoding API

🎯 Runs location tracking in the foreground and background

🔐 Handles permissions for location, DND access, and notifications

🗑 Edit or delete saved locations

🔄 Location updates run continuously while optimized for battery usage

📲 Screens Overview

1. Main Screen

Toggle Tracking On/Off

List of Saved DND Locations

Button to Update or Add New Locations

2. Add/Update Location Screen

Full-screen Google Map

Long press or tap to place marker

"Set Location" button

Dialog to name the location before saving

🧩 App Workflow


graph 

A[Launch App] --> B[Request Permissions]

B --> C[Load Saved Locations]

C --> D[User Enables Tracking]

D --> E[Foreground Service Starts]

E --> F[Location Updates Received]

F --> G[Compare With Geofence Radius]

G --> H{Within Radius?}

H -- Yes --> I[Switch to DND]

H -- No --> J[Normal Mode]

🧠 How It Works

1. Permissions

On launch, the app requests:

POST_NOTIFICATIONS

ACCESS_FINE_LOCATION

ACCESS_BACKGROUND_LOCATION

DND_ACCESS_PERMISSION

Handled via a dedicated permission manager, using system dialogs and explanation alerts.

2. Saving Locations

User taps "Add Location"

Navigates to map screen

Selects a point and enters a name

App saves: id, latitude, longitude, radius, name in Room database

3. Foreground Location Tracking

A Foreground Service runs continuously with a notification

Uses FusedLocationProviderClient to get updated location

Compares the current location to all saved geofences

If within radius, it enables DND Mode via NotificationManager

4. Google Geocoding API

Used to convert lat/lon into a readable place name shown in the UI.

5. Data Handling

Room DAO handles DB read/write

Repository layer handles data logic

ViewModel uses StateFlow to observe saved locations

Background service accesses geofence list through a singleton GeofenceFlowProvider

🔒 Permissions Handled

android.permission.ACCESS_FINE_LOCATION

android.permission.ACCESS_BACKGROUND_LOCATION

android.permission.POST_NOTIFICATIONS

android.permission.ACCESS_NOTIFICATION_POLICY (for DND)

If any are denied, the app shows a dialog with rationale and directs to system settings.

📱 App Screenshots

🏠 Main Screen
<img src="screenshots/mainscreen.jpg" width="500" />

📋 Location List Screen
<img src="screenshots/locationlistscreen.jpg" width="500" />

🗺️ Location Selection Screen
<img src="screenshots/locationselectionscreen.jpg" width="500" />



🧪 Future Improvements

Add geofence circular visualizations on map

Add WorkManager fallback when service is killed

Custom radius selection for each location

Sync saved locations to Firebase for backup

📄 License

This project is licensed under the MIT License. See the LICENSE file for more details.

