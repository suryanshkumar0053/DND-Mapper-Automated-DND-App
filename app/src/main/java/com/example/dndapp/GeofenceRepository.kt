package com.example.dndapp

import kotlinx.coroutines.flow.Flow

class GeofenceRepository(private val geofenceDao: GeofenceDao) {

    // Insert a new geofence into the database
    suspend fun insertGeofence(location: GeofenceLocation) {
        geofenceDao.insertGeofence(location)
    }

    // Return Flow for automatic UI updates
    fun getAllGeofences(): Flow<List<GeofenceLocation>> = geofenceDao.getAllGeofences()

    // Update an existing geofence in the database
    suspend fun updateGeofence(location: GeofenceLocation) {
        geofenceDao.updateGeofence(location)
    }

    // Delete a specific geofence by id from the database
    suspend fun deleteGeofence(geofenceId: Long) {
        geofenceDao.deleteGeofenceById(geofenceId)
    }

    suspend fun getGeofenceById(id: Long): GeofenceLocation? {
        return geofenceDao.getGeofenceById(id)
    }




}
