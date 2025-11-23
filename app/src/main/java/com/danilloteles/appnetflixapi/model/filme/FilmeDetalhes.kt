package com.danilloteles.appnetflixapi.model.filme

import com.danilloteles.appnetflixapi.model.MediaItem

data class FilmeDetalhes(
    val adult: Boolean,
    val backdrop_path: String,
    val belongs_to_collection: Any,
    val budget: Int,
    val genres: List<Genre>,
    val homepage: String,
    val id: Int,
    val imdb_id: String,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val production_companies: List<ProductionCompany>,
    val production_countries: List<ProductionCountry>,
    val release_date: String,
    val revenue: Int,
    val runtime: Int,
    val spoken_languages: List<SpokenLanguage>,
    val status: String,
    val tagline: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

fun FilmeDetalhes.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id,
        overview = this.overview,
        popularity = this.popularity,
        poster_path = this.poster_path,
        backdrop_path = this.backdrop_path,
        vote_average = this.vote_average,
        vote_count = this.vote_count,
        media_type = "movie",
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