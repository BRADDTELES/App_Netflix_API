package com.danilloteles.appnetflixapi.model.v4.response

data class CreateListResponse(
    val status_message: String,
    val id: Int,
    val success: Boolean,
    val status_code: Int
)
