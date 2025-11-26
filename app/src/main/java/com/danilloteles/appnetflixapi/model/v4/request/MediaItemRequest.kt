package com.danilloteles.appnetflixapi.model.v4.request

data class MediaItemRequest(
    val media_type: String, // "movie" ou "tv"
    val media_id: Int
)
