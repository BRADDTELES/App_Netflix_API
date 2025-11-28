package com.danilloteles.appnetflixapi.model.v4.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveItemsV4Response(
    @SerialName("results")
    val results: List<MediaItemV4Response>,
    @SerialName("status_code")
    val status_code: Int,
    @SerialName("status_message")
    val status_message: String,
    @SerialName("success")
    val success: Boolean
)
