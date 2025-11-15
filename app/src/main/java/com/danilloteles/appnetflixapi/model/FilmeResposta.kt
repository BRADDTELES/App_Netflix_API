package com.danilloteles.appnetflixapi.model

data class FilmeResposta(
    val page: Int,
    val results: List<Filme>, // results -> filmes
    val total_pages: Int,
    val total_results: Int
)