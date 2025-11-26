package com.danilloteles.appnetflixapi.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.datasource.paging.minhalista.MyListPagingSource
import com.danilloteles.appnetflixapi.model.ListaDetalhesResposta
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.model.filme.AccountListsResponse
import com.danilloteles.appnetflixapi.model.filme.ListaResposta
import com.danilloteles.appnetflixapi.utils.events.SortOrder
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class MinhaListaRepository(
    val filmeAPI: FilmeAPI,
    val userPreferencesRepository: UserPreferencesRepository
) {
    fun getMyListMoviesStream(listId: String?, sortOrder: SortOrder): Flow<PagingData<MediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MyListPagingSource(filmeAPI, userPreferencesRepository, listId ?: "", sortOrder) }
        ).flow
    }

    suspend fun obterDetalhesDaLista(listId: String, sessionId: String): Response<ListaDetalhesResposta> {
        return filmeAPI.obterDetalhesDaLista(listId, sessionId)
    }

    suspend fun obterListasDaConta(accountId: Int, sessionId: String): Response<AccountListsResponse> {
        return filmeAPI.obterListasDaConta(accountId, sessionId)
    }

    suspend fun removerLista(listId: String, sessionId: String): Response<ListaResposta> {
        return filmeAPI.removerLista(listId, sessionId)
    }
}