package com.danilloteles.appnetflixapi.model.v3

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