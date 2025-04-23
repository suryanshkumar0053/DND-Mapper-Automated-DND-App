package com.example.dndapp


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GeofenceLocation::class], version = 1, exportSchema = false)
abstract class GeofenceDatabase : RoomDatabase() {
    abstract fun geofenceDao(): GeofenceDao

    companion object {
        @Volatile
        private var INSTANCE: GeofenceDatabase? = null

        fun getDatabase(context: Context): GeofenceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeofenceDatabase::class.java,
                    "geofence_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

