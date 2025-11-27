package com.danilloteles.appnetflixapi.model.v4.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddItemsResponse(
    @SerialName("results")
    val results: List<MediaItemResponse>,
    @SerialName("status_code")
    val status_code: Int,
    @SerialName("status_message")
    val status_message: String,
    @SerialName("success")
    val success: Boolean
)