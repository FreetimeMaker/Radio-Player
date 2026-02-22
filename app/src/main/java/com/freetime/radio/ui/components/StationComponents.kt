package com.freetime.radio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.freetime.radio.model.RadioStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    isSearching: Boolean
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search radio stations...") },
        leadingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(query) }
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun StationCard(
    station: RadioStation,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Station Image
            when {
                station.imageResId != 0 -> {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = station.imageResId),
                        contentDescription = station.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                !station.imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = station.imageUrl,
                        contentDescription = station.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        error = painterResource(android.R.drawable.ic_menu_report_image)
                    )
                }
                else -> {
                    // Default placeholder
                    androidx.compose.foundation.Image(
                        painter = painterResource(android.R.drawable.ic_media_play),
                        contentDescription = station.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            
            // Station Info
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPlaying) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (station.countryCode != null || station.languageCode != null) {
                    Text(
                        text = formatCountryAndLanguage(station.countryCode, station.languageCode),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPlaying) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Play Button
            Button(
                onClick = onPlayClick,
                modifier = Modifier.width(80.dp)
            ) {
                Text(if (isPlaying) "Playing" else "Play")
            }
        }
    }
}

@Composable
fun AddStationDialog(
    onDismiss: () -> Unit,
    onAddStation: (name: String, url: String) -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var stationUrl by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Station") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { newValue ->
                        stationName = newValue
                        nameError = if (newValue.isBlank()) "Station name is required" else null
                    },
                    label = { Text("Station Name") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = stationUrl,
                    onValueChange = { newValue ->
                        stationUrl = newValue
                        urlError = if (newValue.isBlank()) "Stream URL is required" else null
                    },
                    label = { Text("Stream URL") },
                    isError = urlError != null,
                    supportingText = urlError?.let { { Text(it) } },
                    singleLine = true,
                    placeholder = { Text("https://example.com/stream.mp3") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hasError = stationName.isBlank() || stationUrl.isBlank()
                    if (!hasError) {
                        onAddStation(stationName.trim(), stationUrl.trim())
                    } else {
                        nameError = if (stationName.isBlank()) "Station name is required" else null
                        urlError = if (stationUrl.isBlank()) "Stream URL is required" else null
                    }
                },
                enabled = stationName.isNotBlank() && stationUrl.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatCountryAndLanguage(countryCode: String?, languageCode: String?): String {
    val parts = mutableListOf<String>()

    countryCode?.let {
        val countryName = try {
            java.util.Locale("", it).getDisplayCountry(java.util.Locale.ENGLISH)
        } catch (e: Exception) {
            it
        }
        parts.add("🌍 $countryName")
    }

    languageCode?.let {
        val languageName = try {
            java.util.Locale(it).getDisplayLanguage(java.util.Locale.ENGLISH)
        } catch (e: Exception) {
            it
        }
        parts.add("🗣️ $languageName")
    }

    return if (parts.isEmpty()) "" else parts.joinToString(" • ")
}
