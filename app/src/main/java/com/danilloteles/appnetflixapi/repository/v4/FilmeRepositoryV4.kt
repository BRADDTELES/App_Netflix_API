package com.danilloteles.appnetflixapi.repository.v4

import com.danilloteles.appnetflixapi.model.v4.request.AddItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.CreateListRequest
import com.danilloteles.appnetflixapi.model.v4.request.MediaItemRequest
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsRequest
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelperV4

class FilmeRepositoryV4 {

    private val apiV4 = RetrofitHelperV4.filmeApiV4

    suspend fun createList(nome: String, descricao: String?) =
        apiV4.createList(CreateListRequest(name = nome, description = descricao))

    suspend fun addMovie(listId: String, movieId: Int) =
        apiV4.addItems(
            listId,
            AddItemsRequest(listOf(MediaItemRequest("movie", movieId)))
        )

    suspend fun addSerie(listId: String, tvId: Int) =
        apiV4.addItems(
            listId,
            AddItemsRequest(listOf(MediaItemRequest("tv", tvId)))
        )

    suspend fun removeMovie(listId: String, movieId: Int) =
        apiV4.removeItems(
            listId,
            RemoveItemsRequest(listOf(MediaItemRequest("movie", movieId)))
        )

    suspend fun removeSerie(listId: String, tvId: Int) =
        apiV4.removeItems(
            listId,
            RemoveItemsRequest(listOf(MediaItemRequest("tv", tvId)))
        )

    suspend fun getListDetails(listId: String) =
        apiV4.getListDetails(listId)
}