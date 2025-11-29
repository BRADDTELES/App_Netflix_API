package com.danilloteles.appnetflixapi.model.video

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    @SerialName("iso_639_1")
    val iso_639_1: String,
    @SerialName("iso_3166_1")
    val iso_3166_1: String,
    @SerialName("name")
    val name: String,
    @SerialName("key")
    val key: String,
    @SerialName("site")
    val site: String,
    @SerialName("size")
    val size: Int,
    @SerialName("type")
    val type: String,
    @SerialName("official")
    val official: Boolean,
    @SerialName("published_at")
    val published_at: String,
    @SerialName("id")
    val videoId: String
)
