package com.freetime.radio

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity auf Portrait-Modus beschränken
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Notification-Berechtigung anfordern (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
    val context = LocalContext.current

    // Notification anzeigen/aktualisieren, wenn sich der aktuelle Sender ändert
    LaunchedEffect(currentStation) {
        currentStation?.let { station ->
            RadioNotificationManager.showNotification(context, station)
        } ?: run {
            RadioNotificationManager.cancelNotification(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🎶 Choose a Station to Play")

        stations.forEach { station ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (station.imageResId != 0) {
                    Image(
                        painter = painterResource(id = station.imageResId),
                        contentDescription = station.name,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text(
                    text = station.name,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    currentStation = station
                    player.setMediaItem(androidx.media3.common.MediaItem.fromUri(station.url))
                    player.prepare()
                    player.playWhenReady = true
                }) {
                    Text("Play")
                }
            }
        }

        currentStation?.let {
            Text("▶ Playing: ${it.name}")
        }
    }
}
