package com.freetime.radio.data

import android.content.Context
import android.content.SharedPreferences
import com.freetime.radio.model.RadioStation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserStationManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("user_stations", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val STATIONS_KEY = "user_stations"
    }
    
    fun getUserStations(): List<RadioStation> {
        val stationsJson = sharedPreferences.getString(STATIONS_KEY, null)
        return if (stationsJson != null) {
            val type = object : TypeToken<List<RadioStation>>() {}.type
            gson.fromJson(stationsJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun addUserStation(station: RadioStation) {
        val currentStations = getUserStations().toMutableList()
        currentStations.add(station)
        saveStations(currentStations)
    }
    
    fun removeUserStation(station: RadioStation) {
        val currentStations = getUserStations().toMutableList()
        currentStations.removeAll { it.url == station.url }
        saveStations(currentStations)
    }
    
    private fun saveStations(stations: List<RadioStation>) {
        val stationsJson = gson.toJson(stations)
        sharedPreferences.edit()
            .putString(STATIONS_KEY, stationsJson)
            .apply()
    }
}
