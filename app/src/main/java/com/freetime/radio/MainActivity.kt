package com.freetime.radio

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import com.freetime.radio.data.RadioStations
import com.freetime.radio.data.loadUserStations
import com.freetime.radio.model.RadioStation
import com.freetime.radio.notification.RadioNotificationManager
import com.freetime.radio.player.RadioPlayerController
import com.freetime.radio.ui.theme.RadioPlayerTheme

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(this).build()

        val baseStations = RadioStations.all
        val userStations = loadUserStations(this)
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
                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(station.url))
                player.prepare()
                player.playWhenReady = true
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (station.imageResId != 0) {
                        Image(
                            painter = painterResource(id = station.imageResId),
                            contentDescription = station.name,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(station.name)
                }
            }
        }

        currentStation?.let {
            Text("▶ Playing: ${it.name}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RadioAppUIPreview() {
    RadioPlayerTheme {
        val context = LocalContext.current
        val fakePlayer = remember { ExoPlayer.Builder(context).build() }
        val sampleStations = listOf(
            RadioStation(
                name = "Sunshine Radio",
                url = "",
                imageResId = 0,
                countryCode = "DE",
                languageCode = "CH"
            ),
            RadioStation(
                name = "Radio Argovia",
                url = "",
                imageResId = 0,
                countryCode = "CH",
                languageCode = "DE"
            )
        )
        RadioAppUI(player = fakePlayer, stations = sampleStations)
    }
}