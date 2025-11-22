package com.danilloteles.appnetflixapi.datasource.paging.filme

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.filme.Filme

class NowPlayingFilmesPagingSource(
    private val filmeAPI: FilmeAPI
) : PagingSource<Int, Filme>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Filme> {
        return try {
            val pagina = params.key ?: 1
            val resposta = filmeAPI.recuperarFilmesTocandoAgora(page = pagina)
            if (resposta.isSuccessful) {
                val filmes = resposta.body()?.results ?: emptyList()
                LoadResult.Page(
                    data = filmes,
                    prevKey = if (pagina == 1) null else pagina - 1,
                    nextKey = if (filmes.isEmpty()) null else pagina + 1
                )
            } else {
                LoadResult.Error(Exception("Falha na resposta da API"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Filme>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}