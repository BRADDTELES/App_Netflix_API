package com.danilloteles.appnetflixapi.model.filme

data class TmdbList(
    val description: String,
    val favorite_count: Int,
    val id: Int,
    val item_count: Int,
    val iso_639_1: String,
    val list_type: String,
    val name: String,
    val poster_path: String?,
    val created_by: String // Opcional, se precisar do criador
)
