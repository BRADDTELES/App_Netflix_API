package com.danilloteles.appnetflixapi.model.v4.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenV4Response(
    @SerialName("access_token")
    val access_token: String,
    @SerialName("account_id")
    val account_id: String,
    @SerialName("status_code")
    val status_code: Int,
    @SerialName("status_message")
    val status_message: String,
    @SerialName("success")
    val success: Boolean
)