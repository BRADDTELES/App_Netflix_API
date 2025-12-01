package com.danilloteles.appnetflixapi.repository.v3

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.serie.PopularSeriesPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.serie.TopRatedSeriesPagingSource
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.model.v3.serie.SerieDetalhes
import com.danilloteles.appnetflixapi.model.video.VideoResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class SerieRepository(
    val filmeAPI: FilmeAPI
) {

    suspend fun recuperarDetalhesSerie(id: Int, language: String): Response<SerieDetalhes> {
        return filmeAPI.recuperarDetalhesSerie(id, "pt-BR")
    }

    suspend fun recuperarVideosSerie(serieId: Int, language: String): Response<VideoResponse> {
        return filmeAPI.recuperarVideosSerie(serieId, "pt-BR")
    }
}