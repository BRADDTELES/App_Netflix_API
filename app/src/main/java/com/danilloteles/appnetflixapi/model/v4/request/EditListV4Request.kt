package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditListV4Request(
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?
)
