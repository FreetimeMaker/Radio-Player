package com.freetime.radio

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.freetime.radio.data.RadioBrowserApiService
import com.freetime.radio.data.RadioStations
import com.freetime.radio.data.loadUserStations
import com.freetime.radio.model.RadioStation
import com.freetime.radio.notification.RadioNotificationManager
import com.freetime.radio.ui.components.StationCard
import com.freetime.radio.ui.components.SearchBar
import com.freetime.radio.ui.components.AddStationDialog
import com.freetime.radio.ui.theme.RadioPlayerTheme
import java.util.Locale
import kotlinx.coroutines.launch

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

        // Restrict activity to portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Request notification permission (Android 13+)
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

        setContent {
            RadioPlayerTheme {
                RadioAppUI(player)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (!hasFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }


    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}

@Composable
fun RadioAppUI(player: ExoPlayer) {
    var currentStation by remember { mutableStateOf<RadioStation?>(null) }
    var stations by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddStationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Load stations from API
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        
        coroutineScope.launch {
            try {
                val result = RadioBrowserApiService.getPopularStations(limit = 30)
                result.onSuccess { apiStations ->
                    val userStations = loadUserStations(context)
                    stations = apiStations + userStations
                    isLoading = false
                }.onFailure { exception ->
                    error = "Failed to load stations: ${exception.message}"
                    // Fallback to local stations
                    val baseStations = RadioStations.all
                    val userStations = loadUserStations(context)
                    stations = baseStations + userStations
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Network error: ${e.message}"
                // Fallback to local stations
                val baseStations = RadioStations.all
                val userStations = loadUserStations(context)
                stations = baseStations + userStations
                isLoading = false
            }
        }
    }
    
    // Search functionality
    fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        
        isSearching = true
        coroutineScope.launch {
            try {
                val result = RadioBrowserApiService.searchByName(query, limit = 20)
                result.onSuccess { results ->
                    searchResults = results
                    isSearching = false
                }.onFailure { exception ->
                    error = "Search failed: ${exception.message}"
                    isSearching = false
                }
            } catch (e: Exception) {
                error = "Search error: ${e.message}"
                isSearching = false
            }
        }
    }
    
    // Notification and on station change
    LaunchedEffect(currentStation) {
        currentStation?.let { station ->
            RadioNotificationManager.showNotification(context, station)
        } ?: run {
            RadioNotificationManager.cancelNotification(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🎶 Radio Player", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
            
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { 
                    performSearch(it)
                    keyboardController?.hide()
                },
                onClear = { 
                    searchQuery = ""
                    searchResults = emptyList()
                },
                isSearching = isSearching
            )
        
            // Current Station Display
            currentStation?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("▶ Playing: ${it.name}", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatCountryAndLanguage(it.countryCode, it.languageCode),
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Station List
            val displayStations = if (searchResults.isNotEmpty()) searchResults else stations
            
            if (isLoading && displayStations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Loading stations...")
                    }
                }
            } else {
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayStations) { station ->
                        StationCard(
                            station = station,
                            isPlaying = currentStation?.url == station.url,
                            onPlayClick = {
                                currentStation = station
                                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(station.url))
                                player.prepare()
                                player.playWhenReady = true
                            }
                        )
                    }
                }
            }
        }
        
        // Floating Action Button for adding stations
        FloatingActionButton(
            onClick = { showAddStationDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Station")
        }
    }
    
    if (showAddStationDialog) {
        AddStationDialog(
            onDismiss = { showAddStationDialog = false },
            onAddStation = { name, url ->
                val newStation = RadioStation(
                    name = name,
                    url = url,
                    imageResId = 0
                )
                stations = stations + newStation
                showAddStationDialog = false
            }
        )
    }
}

fun formatCountryAndLanguage(countryCode: String?, languageCode: String?): String {
    val parts = mutableListOf<String>()

    countryCode?.let {
        val countryName = try {
            Locale("", it).getDisplayCountry(Locale.ENGLISH)
        } catch (e: Exception) {
            it
        }
        parts.add("🌍 $countryName")
    }

    languageCode?.let {
        val languageName = try {
            Locale(it).getDisplayLanguage(Locale.ENGLISH)
        } catch (e: Exception) {
            it
        }
        parts.add("🗣️ $languageName")
    }

    return if (parts.isEmpty()) "" else parts.joinToString(" • ")
}