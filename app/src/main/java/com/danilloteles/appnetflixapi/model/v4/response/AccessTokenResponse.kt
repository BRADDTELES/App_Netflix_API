package com.danilloteles.appnetflixapi.model.v4.response

data class AccessTokenResponse(
    val access_token: String,
    val account_id: String,
    val status_code: Int,
    val status_message: String,
    val success: Boolean
)