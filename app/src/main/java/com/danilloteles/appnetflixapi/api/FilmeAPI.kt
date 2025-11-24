package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.ListaDetalhesResposta
import com.danilloteles.appnetflixapi.model.filme.AccountDetailsResponse
import com.danilloteles.appnetflixapi.model.filme.AccountListsResponse
import com.danilloteles.appnetflixapi.model.filme.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.filme.CreateListRequest
import com.danilloteles.appnetflixapi.model.filme.CreateListResponse
import com.danilloteles.appnetflixapi.model.filme.CreateSessionRequest
import com.danilloteles.appnetflixapi.model.filme.FilmeClassificado
import com.danilloteles.appnetflixapi.model.filme.FilmeDetalhes
import com.danilloteles.appnetflixapi.model.filme.FilmeResposta
import com.danilloteles.appnetflixapi.model.filme.ListaItemResposta
import com.danilloteles.appnetflixapi.model.filme.ListaResposta
import com.danilloteles.appnetflixapi.model.filme.RequestTokenResponse
import com.danilloteles.appnetflixapi.model.filme.SessionIdResponse
import com.danilloteles.appnetflixapi.model.serie.SerieDetalhes
import com.danilloteles.appnetflixapi.model.serie.SerieResposta
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface FilmeAPI {

    @GET("movie/popular")
    suspend fun recuperarFilmesPopulares(
        @Query("page") page: Int
    ): Response<FilmeResposta>

    @GET("movie/{movie_id}")
    suspend fun recuperarDetalhesFilme(
        @Path("movie_id") id: Int
    ): Response<FilmeDetalhes>

    @GET("movie/top_rated")
    suspend fun recuperarFilmesMelhorAvaliados(
        @Query("page") page: Int
    ): Response<FilmeResposta>

    @GET("movie/now_playing")
    suspend fun recuperarFilmesTocandoAgora(
        @Query("page") page: Int
    ): Response<FilmeResposta>

    @GET("account/{account_id}/rated/movies")
    suspend fun recuperarFilmesClassificados(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String
    ): Response<FilmeClassificado>

    @GET("tv/{series_id}")
    suspend fun recuperarDetalhesSerie(
        @Path("series_id") id: Int
    ): Response<SerieDetalhes>

    @GET("tv/popular")
    suspend fun recuperarSeriesPopulares(
        @Query("page") page: Int
    ): Response<SerieResposta>

    @GET("tv/top_rated")
    suspend fun recuperarSeriesMelhoresAvaliados(
        @Query("page") page: Int
    ): Response<SerieResposta>

    // --- Autenticação ---

    @GET("authentication/token/new")
    suspend fun criarTokenDeSolicitacao(): Response<RequestTokenResponse>

    @POST("authentication/session/new")
    suspend fun criarIDdaSessão(
        @Body request: CreateSessionRequest
    ): Response<SessionIdResponse>

    // --- Endpoints para Gerenciamento de Listas e Conta ---

    @GET("account")
    suspend fun obterDetalhesDaConta(
        @Query("session_id") sessionId: String
    ): Response<AccountDetailsResponse>

    @POST("list")
    suspend fun criarLista(
        @Query("session_id") sessionId: String,
        @Body request: CreateListRequest
    ): Response<CreateListResponse>

    @GET("account/{account_id}/lists")
    suspend fun obterListasDaConta(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String
    ): Response<AccountListsResponse>

    @GET("list/{list_id}")
    suspend fun obterDetalhesDaLista(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String
    ): Response<ListaDetalhesResposta>

    @POST("list/{list_id}/add_item")
    suspend fun adicionarItemALista(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String,
        @Body request: AddRemoveListItemRequest
    ): Response<ListaItemResposta>

    @POST("list/{list_id}/remove_item")
    suspend fun removerItemDaLista(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String,
        @Body request: AddRemoveListItemRequest
    ): Response<ListaItemResposta>

    @DELETE("list/{list_id}")
    suspend fun removerLista(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String
    ): Response<ListaResposta>
}