package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.FilmeResposta
import retrofit2.Response
import retrofit2.http.GET

interface FilmeAPI {

    @GET("movie/popular")
    suspend fun recuperarFilmesPopulares(): Response<FilmeResposta>

}