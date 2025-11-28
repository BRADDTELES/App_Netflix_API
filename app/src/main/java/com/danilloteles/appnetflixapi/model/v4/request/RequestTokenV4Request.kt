package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestTokenV4Request(
    @SerialName("redirect_to")
    val redirect_to: String // ex: "netflixapp://auth"
)
