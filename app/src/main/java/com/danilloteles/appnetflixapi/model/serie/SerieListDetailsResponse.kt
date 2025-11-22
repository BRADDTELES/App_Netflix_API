package com.danilloteles.appnetflixapi.model.serie

data class SerieListDetailsResponse(
    val created_by: String,
    val description: String,
    val favorite_count: Int,
    val id: String, // String porque pode ser um ID de lista gerado ou um número
    val item_count: Int,
    val iso_639_1: String,
    val name: String,
    val poster_path: String?,
    val items: List<Serie>,
    val total_pages: Int,
    val total_results: Int
)
