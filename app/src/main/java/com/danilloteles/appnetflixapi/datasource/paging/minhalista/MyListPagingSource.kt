package com.danilloteles.appnetflixapi.datasource.paging.minhalista

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.model.filme.toMediaItem
import com.danilloteles.appnetflixapi.model.serie.toMediaItem
import kotlinx.coroutines.flow.first

class MyListPagingSource(
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listId: String
) : PagingSource<Int, MediaItem>() { // Alterado para MediaItem
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        return try {
            val pagina = params.key ?: 1
            // A API de lista do TMDB v3 não suporta paginação, então só carregamos uma vez.
            if (pagina > 1) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            val sessionId = userPreferencesRepository.sessionId.first()
            val listId = userPreferencesRepository.primaryListId.first()

            if (sessionId == null || listId == null) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            // 1. Obter a lista inicial com IDs (e dados potencialmente corrompidos)
            val initialResponse = filmeAPI.obterDetalhesDaLista(listId, sessionId)
            if (!initialResponse.isSuccessful) {
                return LoadResult.Error(Exception("Falha ao carregar a lista inicial: ${initialResponse.code()}"))
            }

            val itemsFromList = initialResponse.body()?.items ?: emptyList()
            if (itemsFromList.isEmpty()) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            val correctItems = mutableListOf<MediaItem>()

            // 2. Para cada item, buscar os detalhes corretos para contornar o bug da API
            for (item in itemsFromList) {
                try {
                    var added = false

                    // Tentar buscar como série se o media_type for 'tv' ou não for especificado
                    if (item.media_type == "tv" || item.media_type == null) {
                        val serieResponse = filmeAPI.recuperarDetalhesSerie(item.id)
                        if (serieResponse.isSuccessful && serieResponse.body() != null && serieResponse.body()!!.id == item.id) {
                            correctItems.add(serieResponse.body()!!.toMediaItem())
                            added = true
                        }
                    }

                    // Se não foi adicionado como série ou se o media_type for 'movie'
                    if (!added && (item.media_type == "movie" || item.media_type == null)) {
                        val filmeResponse = filmeAPI.recuperarDetalhesFilme(item.id)
                        if (filmeResponse.isSuccessful && filmeResponse.body() != null && filmeResponse.body()!!.id == item.id) {
                            correctItems.add(filmeResponse.body()!!.toMediaItem())
                            added = true
                        }
                    }

                    if (!added) {
                        Log.e("TAG-MyListPagingSource", "Falha ao buscar detalhes para o ID ${item.id} como série ou filme, ou ID de retorno não corresponde.")
                    }
                } catch (e: Exception) {
                    Log.e("TAG-MyListPagingSource", "Exceção ao buscar detalhes para o ID ${item.id}: ${e.message}", e)
                }
            }

            // 3. Retornar a página com os itens corrigidos
            LoadResult.Page(
                data = correctItems,
                prevKey = null, // Não há paginação anterior
                nextKey = null  // Não há próxima página
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? { // Alterado para MediaItem
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}