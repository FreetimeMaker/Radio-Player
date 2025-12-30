package com.freetime.radio.discord

import android.content.Context

object DiscordRPCManager {
    fun start(context: Context) { /* Android unterstützt Discord RPC nicht */ }
    fun stop() { /* nix */ }
    fun updatePresence(stationName: String, songTitle: String?) { /* nix */ }
    fun clearPresence() { /* nix */ }
}
