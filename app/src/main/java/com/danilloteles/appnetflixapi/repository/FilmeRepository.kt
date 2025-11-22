package com.danilloteles.appnetflixapi.repository

import NowPlayingFilmesPagingSource
import TopRatedFilmesPagingSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.filme.PopularFilmesPagingSource
import com.danilloteles.appnetflixapi.model.MediaItem
import kotlinx.coroutines.flow.Flow

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

    fun getTopRatedMoviesStream(): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TopRatedFilmesPagingSource(filmeAPI) }
        ).flow
    }

    fun getNowPlayingMoviesStream(): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { NowPlayingFilmesPagingSource(filmeAPI) }
        ).flow
    }

    fun getMyListMoviesStream(): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            // This still looks wrong as it needs parameters, but leaving as is for now.
            pagingSourceFactory = { PopularFilmesPagingSource(filmeAPI) }
        ).flow
    }
}