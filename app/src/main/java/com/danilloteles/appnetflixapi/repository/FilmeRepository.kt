package com.danilloteles.appnetflixapi.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.filme.PopularFilmesPagingSource
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.model.filme.FilmeDetalhes
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class FilmeRepository(
    val filmeAPI: FilmeAPI
) {
    fun getPopularMoviesStream(): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularFilmesPagingSource(filmeAPI) }
        ).flow
    }

    suspend fun recuperarDetalhesFilme(id: Int): Response<FilmeDetalhes> {
        return filmeAPI.recuperarDetalhesFilme(id)
    }
}
