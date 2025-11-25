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

            Log.d("PagingSource", "[INICIO] Tentando carregar itens para a lista: $listId")

            if (sessionId == null) {
                Log.e("PagingSource", "Session ID nulo. Abortando.")
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            // 1. Obter a lista inicial com IDs
            val initialResponse = filmeAPI.obterDetalhesDaLista(listId, sessionId)
            if (!initialResponse.isSuccessful) {
                Log.e("PagingSource", "Falha ao carregar a lista inicial: ${initialResponse.code()}")
                return LoadResult.Error(Exception("Falha ao carregar a lista inicial: ${initialResponse.code()}"))
            }

            val itemsFromList = initialResponse.body()?.items ?: emptyList()
            Log.d("PagingSource", "Lista inicial recebida com ${itemsFromList.size} itens.")
            if (itemsFromList.isEmpty()) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            val correctItems = mutableListOf<MediaItem>()

            // 2. Para cada item, buscar os detalhes corretos
            for (item in itemsFromList) {
                Log.d("PagingSource", "Processando item com ID: ${item.id} e media_type reportado: ${item.media_type}")
                try {
                    // Tenta buscar como FILME primeiro
                    Log.d("PagingSource", "-> Tentando buscar ID ${item.id} como FILME...")
                    val filmeResponse = filmeAPI.recuperarDetalhesFilme(item.id)
                    if (filmeResponse.isSuccessful && filmeResponse.body() != null) {
                        correctItems.add(filmeResponse.body()!!.toMediaItem())
                        Log.d("PagingSource", "   ... SUCESSO como FILME: '${filmeResponse.body()!!.title}'")
                    } else {
                        Log.d("PagingSource", "   ... FALHA como FILME (Code: ${filmeResponse.code()}).")
                        // Se falhar, tenta buscar como SÉRIE
                        Log.d("PagingSource", "-> Tentando buscar ID ${item.id} como SÉRIE...")
                        val serieResponse = filmeAPI.recuperarDetalhesSerie(item.id)
                        if (serieResponse.isSuccessful && serieResponse.body() != null) {
                            correctItems.add(serieResponse.body()!!.toMediaItem())
                            Log.d("PagingSource", "   ... SUCESSO como SÉRIE: '${serieResponse.body()!!.name}'")
                        } else {
                            Log.w("PagingSource", "   ... FALHA como SÉRIE (Code: ${serieResponse.code()}). Desistindo do ID ${item.id}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PagingSource", "Exceção ao buscar detalhes para o ID ${item.id}: ${e.message}", e)
                }
            }

            Log.d("PagingSource", "[FIM] Processamento concluído. Retornando ${correctItems.size} itens corrigidos.")
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