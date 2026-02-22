package com.freetime.radio.data

import com.freetime.radio.model.RadioStation
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RadioBrowserStation(
    @SerializedName("stationuuid")
    val stationUuid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("homepage")
    val homepage: String,
    @SerializedName("favicon")
    val favicon: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("countrycode")
    val countryCode: String?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("languagecodes")
    val languageCodes: String?,
    @SerializedName("tags")
    val tags: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("votes")
    val votes: Int,
    @SerializedName("clickcount")
    val clickCount: Int
)

object RadioBrowserApiService {
    private const val BASE_URL = "http://de1.api.radio-browser.info/json"
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun searchStations(
        query: String? = null,
        country: String? = null,
        language: String? = null,
        tag: String? = null,
        limit: Int = 50
    ): Result<List<RadioStation>> = withContext(Dispatchers.IO) {
        try {
            val endpoint = when {
                !query.isNullOrBlank() -> "/stations/search?name=$query&limit=$limit&order=clickcount&reverse=true"
                !country.isNullOrBlank() -> "/stations/bycountry/$country?limit=$limit&order=clickcount&reverse=true"
                !language.isNullOrBlank() -> "/stations/bylanguage/$language?limit=$limit&order=clickcount&reverse=true"
                !tag.isNullOrBlank() -> "/stations/bytag/$tag?limit=$limit&order=clickcount&reverse=true"
                else -> "/stations?limit=$limit&order=clickcount&reverse=true"
            }
            
            val url = "$BASE_URL$endpoint"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
            
            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response body"))
            
            val browserStations = gson.fromJson(responseBody, Array<RadioBrowserStation>::class.java)
            
            val radioStations = browserStations.map { station ->
                RadioStation(
                    name = station.name,
                    url = station.url,
                    imageResId = 0, // We'll use imageUrl instead
                    imageUrl = station.favicon,
                    countryCode = station.countryCode,
                    languageCode = extractPrimaryLanguageCode(station.languageCodes)
                )
            }.filter { it.url.isNotBlank() }
            
            Result.success(radioStations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPopularStations(limit: Int = 50): Result<List<RadioStation>> {
        return searchStations(limit = limit)
    }
    
    suspend fun getStationsByCountry(countryCode: String, limit: Int = 30): Result<List<RadioStation>> {
        return searchStations(country = countryCode, limit = limit)
    }
    
    suspend fun getStationsByLanguage(language: String, limit: Int = 30): Result<List<RadioStation>> {
        return searchStations(language = language, limit = limit)
    }
    
    suspend fun searchByName(query: String, limit: Int = 30): Result<List<RadioStation>> {
        return searchStations(query = query, limit = limit)
    }
    
    private fun extractPrimaryLanguageCode(languageCodes: String?): String? {
        return languageCodes?.split(",")?.firstOrNull()?.trim()
    }
}
