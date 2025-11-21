package com.danilloteles.appnetflixapi.datasource.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.Filme
import kotlinx.coroutines.flow.first

class MyListPagingSource(
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listId: String?
) : PagingSource<Int, Filme>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Filme> {
        return try {
            val pagina = params.key ?: 1
            if (pagina > 1) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            val sessionId = userPreferencesRepository.sessionId.first()

            if (sessionId == null || listId == null) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey =  null)
            }

            val response = filmeAPI.obterDetalhesDaLista(listId, sessionId)

            if (response.isSuccessful) {
                val filmes = response.body()?.items ?: emptyList()
                LoadResult.Page(
                    data = filmes,
                    prevKey = null,
                    nextKey = null
                )
            } else {
                LoadResult.Error(Exception("Falha ao carregar Minha Lista: ${response.code()}"))
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