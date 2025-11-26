package com.danilloteles.appnetflixapi.model.v4.response

data class RequestTokenResponse(
    val request_token: String,
    val status_code: Int,
    val status_message: String,
    val success: Boolean
)