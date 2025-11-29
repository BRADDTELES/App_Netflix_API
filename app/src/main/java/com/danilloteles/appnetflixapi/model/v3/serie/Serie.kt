package com.danilloteles.appnetflixapi.model.v3.serie

import com.danilloteles.appnetflixapi.model.v3.MediaItem

data class Serie(
    val backdrop_path: String,
    val first_air_date: String,
    val genre_ids: List<Int>,
    val id: Int,
    val name: String,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val vote_average: Double,
    val vote_count: Int
)

fun Serie.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id,
        overview = this.overview,
        popularity = this.popularity,
        poster_path = this.poster_path,
        backdrop_path = this.backdrop_path,
        vote_average = this.vote_average,
        vote_count = this.vote_count,
        media_type = "tv", // Definido estaticamente
        name = this.name,
        original_name = this.original_name,
        first_air_date = this.first_air_date,
        origin_country = this.origin_country,
        title = null,
        original_title = null,
        release_date = null,
        adult = null,
        video = null
    )
}