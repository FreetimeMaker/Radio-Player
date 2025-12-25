package com.freetime.radio.model

data class RadioStation(
    val name: String,
    val url: String,
    val imageResId: Int,
    val languageCode: String? = null,
    val countryCode: String? = null
)
