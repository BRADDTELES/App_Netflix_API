package com.danilloteles.appnetflixapi.model.v4.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ListDetailsV4Response(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("idioma")
    val iso_639_1: String?,
    @SerialName("average_rating")
    val average_rating: Double?,
    @SerialName("backdrop_path")
    val backdrop_path: String?,
    @SerialName("created_by")
    val created_by: JsonElement?,
    @SerialName("results")
    val results: List<ItemDetailsV4Response>
)