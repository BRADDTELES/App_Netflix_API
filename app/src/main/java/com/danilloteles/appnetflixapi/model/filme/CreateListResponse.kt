package com.danilloteles.appnetflixapi.model.filme

data class CreateListResponse(
    val status_code: Int,
    val status_message: String,
    val success: Boolean,
    val list_id: Int
)
