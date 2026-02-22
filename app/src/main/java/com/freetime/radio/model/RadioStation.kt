package com.freetime.radio.model

data class RadioStation(
    val name: String,
    val url: String,
    val imageResId: Int = 0,
    val imageUrl: String? = null,
    val languageCode: String? = null,
    val countryCode: String? = null
)
