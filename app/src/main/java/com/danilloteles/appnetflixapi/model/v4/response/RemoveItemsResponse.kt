package com.danilloteles.appnetflixapi.model.v4.response

data class RemoveItemsResponse(
    val results: List<MediaItemResponse>,
    val status_code: Int,
    val status_message: String,
    val success: Boolean
)
