package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveItemsRequest(
    @SerialName("items")
    val items: List<MediaItemRequest>
)
