package com.danilloteles.appnetflixapi.model.v3.filme

data class RequestTokenResponse(
    val success: Boolean,
    val expires_at: String,
    val request_token: String
)
