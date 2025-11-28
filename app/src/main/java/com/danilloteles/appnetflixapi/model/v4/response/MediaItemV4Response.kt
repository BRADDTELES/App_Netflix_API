package com.danilloteles.appnetflixapi.model.v4.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaItemV4Response(
    @SerialName("media_id")
    val media_id: Int,
    @SerialName("media_type")
    val media_type: String,
    @SerialName("success")
    val success: Boolean
)