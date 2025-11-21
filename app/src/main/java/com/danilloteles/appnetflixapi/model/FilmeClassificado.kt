package com.danilloteles.appnetflixapi.model

data class FilmeClassificado(
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
)