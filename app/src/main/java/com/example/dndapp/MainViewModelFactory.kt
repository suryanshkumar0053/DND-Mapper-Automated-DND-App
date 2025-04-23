package com.example.dndapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dndapp.ui.theme.MainViewModel

class MainViewModelFactory(
    private val application: Application,
    private val geofenceRepository: GeofenceRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, geofenceRepository,preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
