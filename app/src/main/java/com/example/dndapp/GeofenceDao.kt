package com.example.dndapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {

    @Insert
    suspend fun insertGeofence(location: GeofenceLocation)

    @Update
    suspend fun updateGeofence(location: GeofenceLocation)

    @Query("DELETE FROM geofence_locations WHERE id = :geofenceId")
    suspend fun deleteGeofenceById(geofenceId: Long)


    @Query("SELECT * FROM geofence_locations")
    fun getAllGeofences(): Flow<List<GeofenceLocation>>

    @Query("SELECT * FROM geofence_locations WHERE id = :id")
    suspend fun getGeofenceById(id: Long): GeofenceLocation?

}
