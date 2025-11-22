package com.danilloteles.appnetflixapi.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.serie.PopularSeriesPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.serie.TopRatedSeriesPagingSource
import com.danilloteles.appnetflixapi.model.serie.Serie
import kotlinx.coroutines.flow.Flow

class SerieRepository(
    val filmeAPI: FilmeAPI
) {
    fun getPopularSeriesStream(): Flow<PagingData<Serie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularSeriesPagingSource(filmeAPI) }
        ).flow
    }

    fun getTopRatedMoviesStream(): Flow<PagingData<Serie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TopRatedSeriesPagingSource(filmeAPI) }
        ).flow
    }
}