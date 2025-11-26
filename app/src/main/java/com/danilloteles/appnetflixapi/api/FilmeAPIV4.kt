package com.danilloteles.appnetflixapi.api

import com.danilloteles.appnetflixapi.model.v4.request.AddItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.CreateListRequest
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsRequest
import com.danilloteles.appnetflixapi.model.v4.response.AddItemsResponse
import com.danilloteles.appnetflixapi.model.v4.response.CreateListResponse
import com.danilloteles.appnetflixapi.model.v4.response.ListDetailsResponse
import com.danilloteles.appnetflixapi.model.v4.response.RemoveItemsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FilmeAPIV4 {
    @POST("list")
    suspend fun createList(
        @Body body: CreateListRequest
    ): CreateListResponse

    @POST("list/{list_id}/items")
    suspend fun addItems(
        @Path("list_id") listId: String,
        @Body body: AddItemsRequest
    ): AddItemsResponse

    @DELETE("list/{list_id}/items")
    suspend fun removeItems(
        @Path("list_id") listId: String,
        @Body body: RemoveItemsRequest
    ): RemoveItemsResponse

    @GET("list/{list_id}")
    suspend fun getListDetails(
        @Path("list_id") listId: String
    ): ListDetailsResponse
}