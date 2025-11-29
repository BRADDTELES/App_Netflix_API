package com.danilloteles.appnetflixapi.model.v3.filme

import com.danilloteles.appnetflixapi.model.v3.MediaItem

data class Filme(
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: List<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

fun Filme.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id,
        overview = this.overview,
        popularity = this.popularity,
        poster_path = this.poster_path,
        backdrop_path = this.backdrop_path,
        vote_average = this.vote_average,
        vote_count = this.vote_count,
        media_type = "movie", // Definido estaticamente
        title = this.title,
        original_title = this.original_title,
        release_date = this.release_date,
        adult = this.adult,
        video = this.video,
        name = null,
        original_name = null,
        first_air_date = null,
        origin_country = null
    )
}