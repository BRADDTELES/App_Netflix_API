package com.danilloteles.appnetflixapi.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.filme.PopularFilmesPagingSource
import com.danilloteles.appnetflixapi.model.filme.Filme
import kotlinx.coroutines.flow.Flow

class FilmeRepository(
    val filmeAPI: FilmeAPI
) {
    fun getPopularMoviesStream(): Flow<PagingData<Filme>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularFilmesPagingSource(filmeAPI) }
        ).flow
    }

    fun getTopRatedMoviesStream(): Flow<PagingData<Filme>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularFilmesPagingSource(filmeAPI) }
        ).flow
    }

    fun getNowPlayingMoviesStream(): Flow<PagingData<Filme>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularFilmesPagingSource(filmeAPI) }
        ).flow
    }

    fun getMyListMoviesStream(): Flow<PagingData<Filme>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularFilmesPagingSource(filmeAPI) }
        ).flow
    }
}