package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.FilmeResposta
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FilmeAPI {

    @GET("movie/popular")
    suspend fun recuperarFilmesPopulares(): Response<FilmeResposta>

    @GET("movie/{movie_id}")
    suspend fun recuperarDetalhesFilme(
        @Path("movie_id") id: Int
    ): Response<Filme>

}