package com.danilloteles.appnetflixapi.model.v4.response

data class ListDetailsResponse(
    val id: Int,
    val name: String?,
    val description: String?,
    val iso_639_1: String?,
    val average_rating: Double?,
    val backdrop_path: String?,
    val created_by: Any?,
    val results: List<ItemDetailsResponse>
)