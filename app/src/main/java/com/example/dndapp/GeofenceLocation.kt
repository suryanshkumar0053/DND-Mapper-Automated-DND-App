package com.example.dndapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofence_locations")
data class GeofenceLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,  // User-defined name
    val latitude: Double,
    val longitude: Double,
    val radius: Float = 50f // Default radius is 50 meters
)

data class GeocodingResponse(
    val results: List<Result>
)

data class Result(
    val formatted_address: String
)
