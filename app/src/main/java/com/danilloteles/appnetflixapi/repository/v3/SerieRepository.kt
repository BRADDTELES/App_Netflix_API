package com.danilloteles.appnetflixapi.repository.v3

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.serie.PopularSeriesPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.serie.TopRatedSeriesPagingSource
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.model.video.VideoResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class SerieRepository(
    val filmeAPI: FilmeAPI
) {
    fun getPopularSeriesStream(): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularSeriesPagingSource(filmeAPI) }
        ).flow
    }

    fun getTopRatedSeriesStream(): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TopRatedSeriesPagingSource(filmeAPI) }
        ).flow
    }

    suspend fun recuperarVideosSerie(tvId: Int, language: String): Response<VideoResponse> {
        return filmeAPI.recuperarVideosSerie(tvId, "pt-BR")
    }
}