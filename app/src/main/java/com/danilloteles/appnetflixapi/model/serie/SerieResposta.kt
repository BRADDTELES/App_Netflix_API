package com.danilloteles.appnetflixapi.model.serie

data class SerieResposta(
    val page: Int,
    val results: List<Serie>,
    val total_pages: Int,
    val total_results: Int
)