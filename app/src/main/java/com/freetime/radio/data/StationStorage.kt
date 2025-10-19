package com.freetime.radio.data

import android.content.Context
import com.freetime.radio.model.RadioStation
import com.google.gson.Gson
import java.io.File

fun loadUserStations(context: Context): List<RadioStation> {
    val dir = File(context.filesDir, "stations")
    if (!dir.exists()) return emptyList()

    return dir.listFiles()?.mapNotNull { file ->
        try {
            val json = file.readText()
            Gson().fromJson(json, RadioStation::class.java)
        } catch (e: Exception) {
            null
        }
    } ?: emptyList()
}
