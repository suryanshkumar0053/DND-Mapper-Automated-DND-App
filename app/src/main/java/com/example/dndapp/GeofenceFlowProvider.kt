package com.example.dndapp

import kotlinx.coroutines.flow.StateFlow

object GeofenceFlowProvider {
    private var flow: StateFlow<List<GeofenceLocation>>? = null

    // Set the flow to be accessed later
    fun setFlow(geofencesFlow: StateFlow<List<GeofenceLocation>>) {
        flow = geofencesFlow
    }

    // Get the flow for the service to observe
    fun getFlow(): StateFlow<List<GeofenceLocation>> {
        return flow ?: throw IllegalStateException("Geofence Flow not set")
    }

    // Optionally clear flow when service stops
    fun clear() {
        flow = null
    }
}
