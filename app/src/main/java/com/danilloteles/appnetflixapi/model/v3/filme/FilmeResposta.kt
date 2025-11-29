package com.danilloteles.appnetflixapi.model.v3.filme

data class FilmeResposta(
    val page: Int,
    val results: List<Filme>, // results -> filmes
    val total_pages: Int,
    val total_results: Int
)