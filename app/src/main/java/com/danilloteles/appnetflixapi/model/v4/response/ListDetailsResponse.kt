package com.danilloteles.appnetflixapi.model.v4.response

data class ListDetailsResponse(
    val average_rating: Double,
    val backdrop_path: String,
    val results: List<ItemDetailsResponse>
)