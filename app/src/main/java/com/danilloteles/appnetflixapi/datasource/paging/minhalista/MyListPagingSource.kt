package com.danilloteles.appnetflixapi.datasource.paging.minhalista

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.model.v3.filme.toMediaItem
import com.danilloteles.appnetflixapi.model.v3.serie.toMediaItem
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
                            Log.d("PagingSourceDebug", "Resultado Filme -> id: ${filme?.id}, title: ${filme?.title}, runtime: ${filme?.runtime ?: 0}, budget: ${filme?.budget ?: 0}, revenue: ${filme?.revenue ?: 0}")
                            Log.d("PagingSourceDebug", "Resultado Série -> id: ${serie?.id}, name: ${serie?.name}, seasons: ${serie?.seasons?.size ?: 0}, episode_run_time: ${serie?.episode_run_time?.size ?: 0}, number_of_episodes: ${serie?.number_of_episodes ?: 0}")

                            // Lógica de desambiguação: verifica atributos exclusivos e trata colisões
                            // Prioridade 1: Identificação exclusiva por atributo

                            val isSerieExclusiva = serie != null &&
                                serie.seasons.isNotEmpty() &&
                                serie.episode_run_time.isNotEmpty() &&
                                serie.number_of_episodes > 0 &&
                                (filme == null || (filme.runtime == 0 && filme.budget == 0 && filme.revenue == 0))

                            val isFilmeExclusivo = filme != null &&
                                filme.runtime > 0 &&
                                filme.budget > 0 &&
                                filme.revenue > 0 &&
                                (serie == null || (serie.seasons.isEmpty() && serie.episode_run_time.isEmpty() && serie.number_of_episodes == 0))

                            if (isSerieExclusiva) {
                                Log.d("PagingSourceDebug", "DECISÃO: SÉRIE (identificação exclusiva por múltiplos atributos para ID ${item.id}).")
                                serie.toMediaItem()
                            } else if (isFilmeExclusivo) {
                                Log.d("PagingSourceDebug", "DECISÃO: FILME (identificação exclusiva por múltiplos atributos para ID ${item.id}).")
                                filme.toMediaItem()
                            }
                            // Prioridade 2: Caso de COLISÃO COMPLETA ou ambígua
                            // Se ambos têm atributos exclusivos, priorizamos a SÉRIE devido ao bug.
                            else if (filme != null && filme.runtime > 0 && filme.budget > 0 && filme.revenue > 0 &&
                                     serie != null && serie.seasons.isNotEmpty() && serie.episode_run_time.isNotEmpty() && serie.number_of_episodes > 0) {
                                Log.d("PagingSourceDebug", "DECISÃO: SÉRIE (colisão completa por múltiplos atributos, priorizando SÉRIE devido ao bug da API para ID ${item.id}).")
                                serie.toMediaItem()
                            }
                            // Prioridade 3: Fallback para casos menos claros
                            else if (filme != null) {
                                Log.d("PagingSourceDebug", "DECISÃO: FILME (fallback para ID ${item.id}).")
                                filme.toMediaItem()
                            }
                            else if (serie != null) {
                                Log.d("PagingSourceDebug", "DECISÃO: SÉRIE (fallback para ID ${item.id}).")
                                serie.toMediaItem()
                            }
                            else {
                                Log.e("PagingSourceDebug", "DECISÃO: NENHUM (falha em ambas as buscas para o ID ${item.id}).")
                                null
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
