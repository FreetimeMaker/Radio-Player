package com.freetime.radio.model

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val image: String? = null,
    val language: String? = null
)
