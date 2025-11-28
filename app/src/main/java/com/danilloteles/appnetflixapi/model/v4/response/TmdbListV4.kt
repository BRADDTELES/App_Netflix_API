package com.danilloteles.appnetflixapi.model.v4.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbListV4(
    @SerialName("account_object_id")
    val account_object_id: String,
    @SerialName("adult")
    val adult: Int,
    @SerialName("average_rating")
    val average_rating: Double,
    @SerialName("backdrop_path")
    val backdrop_path: String,
    @SerialName("created_at")
    val created_at: String,
    @SerialName("description")
    val description: String,
    @SerialName("featured")
    val featured: Int,
    @SerialName("id")
    val id: Int,
    @SerialName("iso_3166_1")
    val iso_3166_1: String,
    @SerialName("iso_639_1")
    val iso_639_1: String,
    @SerialName("name")
    val name: String,
    @SerialName("number_of_items")
    val number_of_items: Int,
    @SerialName("poster_path")
    val poster_path: String,
    @SerialName("public")
    val `public`: Int,
    @SerialName("revenue")
    val revenue: String,
    @SerialName("runtime")
    val runtime: Int,
    @SerialName("sort_by")
    val sort_by: Int,
    @SerialName("updated_at")
    val updated_at: String
)