package com.danilloteles.appnetflixapi.model.filme

data class RequestTokenResponse(
    val success: Boolean,
    val expires_at: String,
    val request_token: String
)
