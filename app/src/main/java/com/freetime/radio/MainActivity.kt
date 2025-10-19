package com.freetime.radio

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import com.freetime.radio.data.loadUserStations
import com.freetime.radio.model.RadioStation
import com.freetime.radio.notification.RadioNotificationManager
import com.freetime.radio.player.RadioPlayerController
import com.freetime.radio.ui.theme.RadioPlayerTheme

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RadioNotificationManager.init(this)

        val station = RadioStation("Sunshine Radio", "https://chmedia.streamabc.net/79-rsunshine-mp3-192-4746851?sABC=68s4q152%231%231699795900047_8633080%23puzrqvn-enqvb-jro&aw_0_1st.playerid=chmedia-radio-web&amsparams=playerid:chmedia-radio-web;skey:1760874834")
        RadioNotificationManager.playStream(this, station)
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}

@Composable
fun RadioAppUI(player: ExoPlayer, stations: List<RadioStation>) {
    var currentStation by remember { mutableStateOf<RadioStation?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎶 Choose a Station to Play")

        stations.forEach { station ->
            Button(onClick = {
                currentStation = station
                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(station.streamUrl))
                player.prepare()
                player.playWhenReady = true
            }) {
                Text(station.name)
            }
        }

        currentStation?.let {
            Text("▶ Playing: ${it.name}")
        }
    }
}
