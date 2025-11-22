package com.danilloteles.appnetflixapi.model

// Modelo unificado para Filme ou Série
data class MediaItem(
    // Campos comuns
    val id: Int,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val media_type: String, // "movie" ou "tv"

    // Campos específicos (nuláveis)
    val title: String?, // de Filme
    val original_title: String?, // de Filme
    val release_date: String?, // de Filme
    val adult: Boolean?, // de Filme
    val video: Boolean?, // de Filme

    val name: String?, // de Série
    val original_name: String?, // de Série
    val first_air_date: String?, // de Série
    val origin_country: List<String>? // de Série
)

// Resposta unificada para detalhes da lista
data class ListDetailsResponse(
    val created_by: String,
    val description: String,
    val favorite_count: Int,
    val id: String,
    val item_count: Int,
    val iso_639_1: String,
    val name: String,
    val poster_path: String?,
    val items: List<MediaItem> // Usa a lista de MediaItem
)
