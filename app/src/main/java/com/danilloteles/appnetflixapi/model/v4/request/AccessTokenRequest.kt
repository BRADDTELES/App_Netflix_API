package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenRequest(
    @SerialName("request_token")
    val request_token: String
)
