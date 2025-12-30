package com.freetime.radio.discord

import android.content.Context
import android.util.Log
import com.freetime.radio.model.RadioStation
import dev.kizzy.rpc.KizzyRPC

object DiscordRPCManager {
    private var kizzyRPC: KizzyRPC? = null
    private const val DISCORD_APP_ID = "1455379422558556160" // TODO: Ersetzen Sie dies mit Ihrer Discord App ID

    fun initialize(context: Context) {
        if (kizzyRPC == null) {
            try {
                kizzyRPC = KizzyRPC(context, DISCORD_APP_ID)
                kizzyRPC?.start()
                Log.d("DiscordRPCManager", "Discord RPC initialized successfully.")
            } catch (e: Exception) {
                // Discord ist möglicherweise nicht installiert oder nicht verfügbar
                Log.e("DiscordRPCManager", "Failed to initialize Discord RPC", e)
                e.printStackTrace()
            }
        }
    }

    fun updatePresence(station: RadioStation) {
        try {
            val countryInfo = station.countryCode?.let { "🌍 $it" } ?: ""
            val languageInfo = station.languageCode?.let { "🗣️ $it" } ?: ""
            val details = listOfNotNull(countryInfo, languageInfo).joinToString(" • ")
            
            kizzyRPC?.updatePresence(
                state = "Hört Radio",
                details = "▶ ${station.name}${if (details.isNotEmpty()) " • $details" else ""}",
                largeImageKey = "radio_icon",
                largeImageText = "Radio Player"
            )
            Log.d("DiscordRPCManager", "Updated Discord presence for station: ${station.name}")
        } catch (e: Exception) {
            Log.e("DiscordRPCManager", "Failed to update Discord presence", e)
            e.printStackTrace()
        }
    }

    fun clearPresence() {
        try {
            kizzyRPC?.updatePresence(
                state = "Inaktiv",
                details = "Kein Sender ausgewählt",
                largeImageKey = "radio_icon",
                largeImageText = "Radio Player"
            )
            Log.d("DiscordRPCManager", "Cleared Discord presence.")
        } catch (e: Exception) {
            Log.e("DiscordRPCManager", "Failed to clear Discord presence", e)
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            kizzyRPC?.stop()
            kizzyRPC = null
            Log.d("DiscordRPCManager", "Discord RPC stopped.")
        } catch (e: Exception) {
            Log.e("DiscordRPCManager", "Failed to stop Discord RPC", e)
            e.printStackTrace()
        }
    }
}