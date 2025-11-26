package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.v4.request.AccessTokenRequest
import com.danilloteles.appnetflixapi.model.v4.request.AddItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.CreateListRequest
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.RequestTokenRequest
import com.danilloteles.appnetflixapi.model.v4.response.AccessTokenResponse
import com.danilloteles.appnetflixapi.model.v4.response.AddItemsResponse
import com.danilloteles.appnetflixapi.model.v4.response.CreateListResponse
import com.danilloteles.appnetflixapi.model.v4.response.ListDetailsResponse
import com.danilloteles.appnetflixapi.model.v4.response.RemoveItemsResponse
import com.danilloteles.appnetflixapi.model.v4.response.RequestTokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FilmeAPIV4 {
    @POST("auth/request_token")
    suspend fun createRequestToken(
        @Body body: RequestTokenRequest
    ): RequestTokenResponse

    @POST("auth/access_token")
    suspend fun createAccessToken(
        @Body body: AccessTokenRequest
    ): AccessTokenResponse

    @POST("list")
    suspend fun createList(
        @Header("Authorization") authorization: String,
        @Body body: CreateListRequest
    ): CreateListResponse

    @POST("list/{list_id}/items")
    suspend fun addItems(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String,
        @Body body: AddItemsRequest
    ): AddItemsResponse

    /*@DELETE("list/{list_id}/items")*/ // No Retrofit @DELETE não pode usar @Body → usar @HTTP
    @HTTP(method = "DELETE", path = "list/{list_id}/items", hasBody = true)
    suspend fun removeItems(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String,
        @Body body: RemoveItemsRequest
    ): RemoveItemsResponse

    @GET("list/{list_id}")
    suspend fun getListDetails(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String
    ): ListDetailsResponse
}