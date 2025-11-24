package com.danilloteles.appnetflixapi.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.datasource.paging.minhalista.MyListPagingSource
import com.danilloteles.appnetflixapi.model.MediaItem
import kotlinx.coroutines.flow.Flow

class MinhaListaRepository(
    val filmeAPI: FilmeAPI,
    val userPreferencesRepository: UserPreferencesRepository
) {
    fun getMyListMoviesStream(listId: String?): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MyListPagingSource(filmeAPI, userPreferencesRepository, listId ?: "") }
        ).flow
    }
}