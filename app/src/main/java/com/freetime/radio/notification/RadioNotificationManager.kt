package com.freetime.radio.notification

import android.Manifest
import android.app.*
import android.content.*
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.freetime.radio.R
import com.freetime.radio.model.RadioStation

object RadioNotificationManager {
    private const val CHANNEL_ID = "radio_channel"
    private const val NOTIFICATION_ID = 1001

    private var player: ExoPlayer? = null
    private var currentStation: RadioStation? = null

    fun init(context: Context) {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
            createChannel(context)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun playStream(context: Context, station: RadioStation) {
        if (station.url != currentStation?.url) {
            val mediaItem = MediaItem.fromUri(station.url)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            currentStation = station
        }
        player?.playWhenReady = true
        showNotification(context)
    }

    fun play() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun stop(context: Context) {
        player?.stop()
        currentStation = null
        cancelNotification(context)
    }

    fun release() {
        player?.release()
        player = null
        currentStation = null
    }

    fun isPlaying(): Boolean = player?.isPlaying == true

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Control your radio stream"
                enableLights(false)
                enableVibration(false)
            }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context) {
        val station = currentStation ?: return
        val isPlaying = isPlaying()

        val playIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = if (isPlaying) "ACTION_PAUSE" else "ACTION_PLAY"
        }
        val playPendingIntent = PendingIntent.getBroadcast(
            context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_STOP"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("🎶 Now Playing: ${station.name}")
            .setContentText("Control playback from here")
            .setSmallIcon(R.drawable.ic_radio)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPendingIntent
            )
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setStyle(MediaStyle())
            .setOngoing(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_PLAY" -> RadioNotificationManager.play()
            "ACTION_PAUSE" -> RadioNotificationManager.pause()
            "ACTION_STOP" -> RadioNotificationManager.stop(context)
        }
    }
}
