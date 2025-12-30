package com.freetime.radio.discord

import android.content.Context
import com.freetime.radio.model.RadioStation
import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.data.activity.timestamps
import kotlinx.coroutines.*

object DiscordRPCManager {

    private const val CLIENT_ID = "1455379422558556160"

    private var ipc: KDiscordIPC? = null
    private var scope: CoroutineScope? = null
    private var isConnected = false

    fun start(context: Context) {
        if (isConnected) return

        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope?.launch {
            try {
                ipc = KDiscordIPC(CLIENT_ID)
                ipc?.connect()
                isConnected = true
                println("Discord RPC started")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        scope?.cancel()
        scope = null
        ipc = null
        isConnected = false
    }

    fun updatePresence(stationName: String, songTitle: String?) {
        if (!isConnected) return
        scope ?: return

        scope?.launch(Dispatchers.IO) {
            try {
                ipc?.activityManager?.setActivity(
                    details = songTitle ?: "Listens to $stationName",
                    state = "Radio Player (JetCom)"
                ) {
                    timestamps(System.currentTimeMillis(), null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearPresence() {
        if (!isConnected) return
        scope ?: return

        scope?.launch(Dispatchers.IO) {
            try {
                ipc?.activityManager?.clearActivity()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
