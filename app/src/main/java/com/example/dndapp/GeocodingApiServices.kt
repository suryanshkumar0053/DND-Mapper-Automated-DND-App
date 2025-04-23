package com.example.dndapp

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("geocode/json")
    suspend fun getAddress(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeocodingResponse

    companion object {
        fun create(): GeocodingApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    Log.d("GeocodingApiService", "📤 Requesting: ${request.url()}") // ✅ Use `url()`
                    val response = chain.proceed(request)
                    Log.d("GeocodingApiService", "📥 Response Code: ${response.code()}") // ✅ Use `code()`
                    response
                }
                .build()



            return Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client) // Attach logging
                .build()
                .create(GeocodingApiService::class.java)
        }
    }
}

