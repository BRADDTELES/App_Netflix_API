package com.danilloteles.appnetflixapi.model.filme

data class FilmeClassificado(
    val page: Int,
    val movies: List<Movie>,
    val total_pages: Int,
    val total_results: Int
)