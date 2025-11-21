package com.danilloteles.appnetflixapi.api

// Imports para os novos modelos de dados
import com.danilloteles.appnetflixapi.model.AccountDetailsResponse
import com.danilloteles.appnetflixapi.model.AccountListsResponse
import com.danilloteles.appnetflixapi.model.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.CreateListRequest
import com.danilloteles.appnetflixapi.model.CreateListResponse
import com.danilloteles.appnetflixapi.model.CreateSessionRequest
import com.danilloteles.appnetflixapi.model.FilmeClassificado
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.model.FilmeResposta
import com.danilloteles.appnetflixapi.model.ListDetailsResponse
import com.danilloteles.appnetflixapi.model.ListItemResponse
import com.danilloteles.appnetflixapi.model.RequestTokenResponse
import com.danilloteles.appnetflixapi.model.SessionIdResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface FilmeAPI {

    @GET("movie/popular")
    suspend fun recuperarFilmesPopulares(): Response<FilmeResposta>

    @GET("movie/{movie_id}")
    suspend fun recuperarDetalhesFilme(
        @Path("movie_id") id: Int
    ): Response<FilmeDetalhes>

    @GET("movie/top_rated")
    suspend fun recuperarFilmesMelhorAvaliados(): Response<FilmeResposta>

    @GET("movie/now_playing")
    suspend fun recuperarFilmesTocandoAgora(): Response<FilmeResposta>

    @GET("account/{account_id}/rated/movies")
    suspend fun recuperarFilmesClassificados(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String
    ): Response<FilmeClassificado>

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

    @GET("account/{account_id}/lists")
    suspend fun obterListasDeContas(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String
    ): Response<AccountListsResponse>

    @POST("list")
    suspend fun criarLista(
        @Query("session_id") sessionId: String,
        @Body request: CreateListRequest
    ): Response<CreateListResponse>

    @GET("list/{list_id}")
    suspend fun obterDetalhesDaLista(
        @Path("list_id") listId: String, // list_id pode ser String
        @Query("session_id") sessionId: String
    ): Response<ListDetailsResponse>

    @POST("list/{list_id}/add_item")
    suspend fun adicionarFilmeALista(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String,
        @Body request: AddRemoveListItemRequest
    ): Response<ListItemResponse>

    @POST("list/{list_id}/remove_item")
    suspend fun removerFilmeDaLista(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String,
        @Body request: AddRemoveListItemRequest
    ): Response<ListItemResponse>
}