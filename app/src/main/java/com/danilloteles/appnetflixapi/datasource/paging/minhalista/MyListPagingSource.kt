package com.danilloteles.appnetflixapi.datasource.paging.minhalista

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.model.filme.toMediaItem
import com.danilloteles.appnetflixapi.model.serie.toMediaItem
import com.danilloteles.appnetflixapi.utils.events.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class MyListPagingSource(
    private val filmeAPI: FilmeAPI,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val listId: String?,
    private val sortOrder: SortOrder
) : PagingSource<Int, MediaItem>() { // Alterado para MediaItem
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        return try {
            val pagina = params.key ?: 1
            // A API de lista do TMDB v3 não suporta paginação, então só carregamos uma vez.
            if (pagina > 1) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            val sessionId = userPreferencesRepository.sessionId.first()
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
            // Usando coroutines para fazer chamadas em paralelo e desambiguar com segurança
            coroutineScope {
                val jobs = itemsFromList.map { item ->
                    async(Dispatchers.IO) {
                        try {
                            val filmeDeferred = async { filmeAPI.recuperarDetalhesFilme(item.id) }
                            val serieDeferred = async { filmeAPI.recuperarDetalhesSerie(item.id) }

                            val filmeResponse = filmeDeferred.await()
                            val serieResponse = serieDeferred.await()

                            val filme = if (filmeResponse.isSuccessful) filmeResponse.body() else null
                            val serie = if (serieResponse.isSuccessful) serieResponse.body() else null

                            Log.d("PagingSourceDebug", "--- Desambiguando ID: ${item.id} ---")
                            Log.d("PagingSourceDebug", "Item Original -> media_type: ${item.media_type}, title: ${item.title}, name: ${item.name}")
                            Log.d("PagingSourceDebug", "Resultado Filme -> title: ${filme?.title}")
                            Log.d("PagingSourceDebug", "Resultado Série -> name: ${serie?.name}")

                            when {
                                // Caso 1: Ambos existem, precisamos desempatar
                                filme != null && serie != null -> {
                                    Log.d("PagingSourceDebug", "COLISÃO DETECTADA. Iniciando desempate.")

                                    // HEURÍSTICA AGRESSIVA:
                                    // Se o item original da lista foi classificado como "movie" (o cenário do bug da API),
                                    // e uma série com o mesmo ID também foi encontrada, vamos assumir que é a série
                                    // que o usuário queria, pois a API de lista corrompeu os dados (media_type, title, etc).
                                    if (item.media_type == "movie") {
                                        Log.d("PagingSourceDebug", "DECISÃO: SÉRIE (Heurística: media_type 'movie' em colisão -> prioriza série).")
                                        serie.toMediaItem()
                                    } else {
                                        // Para outros casos (ex: media_type 'tv' ou nulo), usamos a lógica de correspondência de nome/título.
                                        val originalTitleOrName = item.name ?: item.title
                                        val filmeMatch = originalTitleOrName.equals(filme.title, ignoreCase = true)
                                        val serieMatch = originalTitleOrName.equals(serie.name, ignoreCase = true)
                                        Log.d("PagingSourceDebug", "Comparando '${originalTitleOrName}' -> filmeMatch: $filmeMatch, serieMatch: $serieMatch")

                                        if (serieMatch && !filmeMatch) {
                                            Log.d("PagingSourceDebug", "DECISÃO: SÉRIE (correspondência de nome exclusiva).")
                                            serie.toMediaItem()
                                        } else if (filmeMatch && !serieMatch) {
                                            Log.d("PagingSourceDebug", "DECISÃO: FILME (correspondência de título exclusiva).")
                                            filme.toMediaItem()
                                        } else { // Casos mais ambíguos (ambos batem ou nenhum bate)
                                            Log.d("PagingSourceDebug", "Desempate ambíguo. Priorizando série como fallback para corrigir bug da API.")
                                            serie.toMediaItem() // Em caso de dúvida total, ainda priorizamos a série.
                                        }
                                    }
                                }
                                // Caso 2: Apenas o filme existe
                                filme != null -> {
                                    Log.d("PagingSourceDebug", "DECISÃO: FILME (único encontrado).")
                                    filme.toMediaItem()
                                }
                                // Caso 3: Apenas a série existe
                                serie != null -> {
                                    Log.d("PagingSourceDebug", "DECISÃO: SÉRIE (única encontrada).")
                                    serie.toMediaItem()
                                }
                                // Caso 4: Nenhum existe
                                else -> {
                                    Log.e("PagingSourceDebug", "DECISÃO: NENHUM (falha em ambas as buscas para o ID ${item.id}).")
                                    null
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PagingSourceDebug", "Exceção ao buscar detalhes para o ID ${item.id}: ${e.message}", e)
                            null
                        }
                    }
                }
                // Aguarda todos os jobs e coleta os resultados não nulos
                correctItems.addAll(jobs.awaitAll().filterNotNull())
            }

            // 3. Aplicar a ordenação
            val sortedItems = when (sortOrder) {
                SortOrder.TITLE_ASC -> correctItems.sortedBy { it.title ?: it.name }
                else -> correctItems
            }

            // 4. Retornar a página com os itens corrigidos e ordenados
            LoadResult.Page(
                data = sortedItems,
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
