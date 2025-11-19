package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.CreateSessionRequest
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.model.FilmeResposta
import com.danilloteles.appnetflixapi.model.RequestTokenResponse
import com.danilloteles.appnetflixapi.model.SessionIdResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FilmeAPI {

    @GET("movie/popular")
    suspend fun recuperarFilmesPopulares(): Response<FilmeResposta>

    @GET("movie/{movie_id}")
    suspend fun recuperarDetalhesFilme(
        @Path("movie_id") id: Int
    ): Response<FilmeDetalhes>

    @GET("authentication/token/new")
    suspend fun createRequestToken(): Response<RequestTokenResponse>

    @POST("authentication/session/new")
    suspend fun createSessionId(
        @Body request: CreateSessionRequest
    ): Response<SessionIdResponse>

}