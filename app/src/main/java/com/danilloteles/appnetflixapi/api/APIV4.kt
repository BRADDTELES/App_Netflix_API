package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.v4.request.AccessTokenV4Request
import com.danilloteles.appnetflixapi.model.v4.request.AddItemsV4Request
import com.danilloteles.appnetflixapi.model.v4.request.CreateListV4Request
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsV4Request
import com.danilloteles.appnetflixapi.model.v4.request.RequestTokenV4Request
import com.danilloteles.appnetflixapi.model.v4.response.AccessTokenV4Response
import com.danilloteles.appnetflixapi.model.v4.response.AddItemsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.CreateListV4Response
import com.danilloteles.appnetflixapi.model.v4.response.ListDetailsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RemoveItemsV4Response
import com.danilloteles.appnetflixapi.model.filme.AccountListsResponse
import com.danilloteles.appnetflixapi.model.v4.response.AccountListsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RemoveListV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RequestTokenV4Response
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface APIV4 {
    @POST("auth/request_token")
    suspend fun createRequestToken(
        @Body body: RequestTokenV4Request
    ): RequestTokenV4Response

    @POST("auth/access_token")
    suspend fun createAccessToken(
        @Body body: AccessTokenV4Request
    ): AccessTokenV4Response

    @POST("list")
    suspend fun createList(
        @Header("Authorization") authorization: String,
        @Body body: CreateListV4Request
    ): CreateListV4Response

    @GET("account/{account_object_id}/lists")
    suspend fun getAccountLists(
        @Header("Authorization") authorization: String,
        @Path("account_object_id") accountObjectId: String
    ): Response<AccountListsV4Response>

    @GET("list/{list_id}")
    suspend fun getListDetails(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String
    ): ListDetailsV4Response

    @DELETE("list/{list_id}")
    suspend fun removeList(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String
    ): Response<RemoveListV4Response>

    @POST("list/{list_id}/items")
    suspend fun addItems(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String,
        @Body body: AddItemsV4Request
    ): AddItemsV4Response

    @HTTP(method = "DELETE", path = "list/{list_id}/items", hasBody = true)
    suspend fun removeItems(
        @Header("Authorization") authorization: String,
        @Path("list_id") listId: String,
        @Body body: RemoveItemsV4Request
    ): RemoveItemsV4Response
}