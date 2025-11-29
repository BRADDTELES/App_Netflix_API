package com.danilloteles.appnetflixapi.model.v3.serie

data class SerieResposta(
    val page: Int,
    val results: List<Serie>,
    val total_pages: Int,
    val total_results: Int
)