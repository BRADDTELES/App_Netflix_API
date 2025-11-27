package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaItemRequest(
    @SerialName("media_type")
    val media_type: String, // "movie" ou "tv"
    @SerialName("media_id")
    val media_id: Int
)
