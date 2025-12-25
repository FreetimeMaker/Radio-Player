package com.freetime.radio.data

import com.freetime.radio.R
import com.freetime.radio.model.RadioStation

object RadioStations {
    val all = listOf(
        RadioStation(
            name = "Sunshine Radio",
            url = "https://chmedia.streamabc.net/79-rsunshine-mp3-192-4746851?sABC=68s4q152%231%231699795900047_8633080%23puzrqvn-enqvb-jro&aw_0_1st.playerid=chmedia-radio-web&amsparams=playerid:chmedia-radio-web;skey:1760874834",
            imageResId = R.drawable.sunshine_logo, // Assuming you have this drawable
            countryCode = "DE",
            languageCode = "CH"
        ),
        RadioStation(
            name = "Radio Argovia",
            url = "https://chmedia.streamabc.net/79-argovia-mp3-192-3024993?sABC=68s5qo3r%230%23q7s809s74070n543n7p5213q6qr39235%23qverpg&aw_0_1st.playerid=direct&amsparams=playerid:direct;skey:1760942910",
            imageResId = R.drawable.argovia_logo, // Placeholder, ensure you have this drawable
            countryCode = "CH",
            languageCode = "DE"
        ),
        RadioStation(
            name = "Radio Pilatus",
            url = "https://chmedia.streamabc.net/79-pilatus-mp3-192-4664468?sABC=68s5s266%230%23q7s809s74070n543n7p5213q6qr39235%23puzrqvn&aw_0_1st.playerid=chmedia&amsparams=playerid:chmedia;skey:1760948838",
            imageResId = R.drawable.pilatus_logo, // Placeholder, ensure you have this drawable
            countryCode = "CH",
            languageCode = "DE"
        )
    )
}