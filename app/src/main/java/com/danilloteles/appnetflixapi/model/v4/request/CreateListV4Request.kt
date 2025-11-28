package com.danilloteles.appnetflixapi.model.v4.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateListV4Request(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,
    @SerialName("idioma")
    val iso_639_1: String = "pt-BR"
)
