package com.freetime.radio.discord

import android.content.Context
import com.freetime.radio.model.RadioStation
import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.core.event.Event
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
                val client = KDiscordIPC(CLIENT_ID)

                client.on<Event> {
                    println("Discord RPC connected as ${data.user.username}#${data.user.discriminator}")
                }

                client.connect()
                ipc = client
                isConnected = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        scope?.launch {
            try {
                ipc?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                ipc = null
                isConnected = false
            }
        }
        scope?.cancel()
        scope = null
    }

    fun updatePresence(stationName: String, songTitle: String?) {
        val client = ipc ?: return
        val s = scope ?: return

        s.launch {
            try {
                client.activityManager.setActivity(
                    details = songTitle ?: "Listens to $stationName",
                    state = "Radio Player (JetCom)",
                ) {
                    timestamps(System.currentTimeMillis(), null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearPresence() {
        val client = ipc ?: return
        val s = scope ?: return

        s.launch {
            try {
                client.activityManager.clearActivity()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}