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
import retrofit2.http.Query // Import adicionado

// Imports para os novos modelos de dados
import com.danilloteles.appnetflixapi.model.AccountDetailsResponse
import com.danilloteles.appnetflixapi.model.AccountListsResponse
import com.danilloteles.appnetflixapi.model.CreateListRequest
import com.danilloteles.appnetflixapi.model.CreateListResponse
import com.danilloteles.appnetflixapi.model.ListDetailsResponse
import com.danilloteles.appnetflixapi.model.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.ListItemResponse


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

    // --- Endpoints para Gerenciamento de Listas e Conta ---

    @GET("account")
    suspend fun getAccountDetails(
        @Query("session_id") sessionId: String
    ): Response<AccountDetailsResponse>

    @GET("account/{account_id}/lists")
    suspend fun getAccountLists(
        @Path("account_id") accountId: Int,
        @Query("session_id") sessionId: String
    ): Response<AccountListsResponse>

    @POST("list")
    suspend fun createList(
        @Query("session_id") sessionId: String,
        @Body request: CreateListRequest
    ): Response<CreateListResponse>

    @GET("list/{list_id}")
    suspend fun getListDetails(
        @Path("list_id") listId: String, // list_id pode ser String
        @Query("session_id") sessionId: String
    ): Response<ListDetailsResponse>

    @POST("list/{list_id}/add_item")
    suspend fun addMovieToList(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String,
        @Body request: AddRemoveListItemRequest
    ): Response<ListItemResponse>

    @POST("list/{list_id}/remove_item")
    suspend fun removeMovieFromList(
        @Path("list_id") listId: String,
        @Query("session_id") sessionId: String,
        @Body request: AddRemoveListItemRequest
    ): Response<ListItemResponse>
}