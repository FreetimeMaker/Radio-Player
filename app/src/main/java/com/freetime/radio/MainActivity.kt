package com.freetime.radio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.freetime.radio.data.RadioStations
import com.freetime.radio.data.loadUserStations
import com.freetime.radio.model.RadioStation
import com.freetime.radio.ui.theme.RadioPlayerTheme

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseStations = RadioStations.all
        val userStations = loadUserStations(this) // optional
        val allStations = baseStations + userStations

        setContent {
            RadioAppUI(player, allStations)
        }
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
