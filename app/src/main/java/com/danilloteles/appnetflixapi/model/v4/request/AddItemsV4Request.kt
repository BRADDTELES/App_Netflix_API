package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddItemsV4Request(
    @SerialName("items")
    val items: List<MediaItemV4Request>
)
