package com.danilloteles.appnetflixapi.datasource.paging.serie

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.serie.Serie

class PopularSeriesPagingSource(
    private val filmeAPI: FilmeAPI
) : PagingSource<Int, Serie>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Serie> {
        return try {
            val pagina = params.key ?: 1
            val resposta = filmeAPI.recuperarSeriesPopulares(page = pagina)
            if (resposta.isSuccessful) {
                val series = resposta.body()?.results ?: emptyList()
                LoadResult.Page(
                    data = series,
                    prevKey = if (pagina == 1) null else pagina - 1,
                    nextKey = if (series.isEmpty()) null else pagina + 1
                )
            } else {
                LoadResult.Error(Exception("Falha na resposta da API"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Serie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}