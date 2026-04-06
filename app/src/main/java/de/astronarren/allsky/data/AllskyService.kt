package de.astronarren.allsky.data

import retrofit2.http.GET

interface AllskyService {
    @GET("videos/")
    suspend fun getTimelapses(): String

    @GET("keograms/")
    suspend fun getKeograms(): String

    @GET("startrails/")
    suspend fun getStartrails(): String

    @GET("images/")
    suspend fun getImages(): String

    @GET("meteors/")
    suspend fun getMeteors(): String
}

data class AllskyContent(
    val timelapses: List<AllskyMedia>,
    val keograms: List<AllskyMedia>,
    val startrails: List<AllskyMedia>,
    val images: List<AllskyMedia> = emptyList(),
    val meteors: List<AllskyMedia> = emptyList()
)

data class AllskyMedia(
    val date: String,
    val url: String
) 