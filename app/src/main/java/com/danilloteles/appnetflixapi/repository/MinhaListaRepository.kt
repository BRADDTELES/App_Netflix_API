package com.danilloteles.appnetflixapi.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.api.FilmeAPIV4
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.datasource.paging.minhalista.MyListPagingSource
import com.danilloteles.appnetflixapi.model.ListaDetalhesResposta
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.model.filme.AccountListsResponse
import com.danilloteles.appnetflixapi.model.filme.ListaResposta
import com.danilloteles.appnetflixapi.model.v4.response.RemoveListResponse
import com.danilloteles.appnetflixapi.utils.events.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.io.IOException

class MinhaListaRepository(
    private val filmeAPI: FilmeAPI,
    private val filmeAPIV4: FilmeAPIV4, // Injetando a API v4
    private val userPreferencesRepository: UserPreferencesRepository
) {

    // --- Métodos V4 ---

    fun obterListasDaContaV4(accountId: String, accessToken: String): Flow<Result<AccountListsResponse>> = flow {
        emit(safeApiCall { filmeAPIV4.getAccountLists(accessToken, accountId) })
    }.flowOn(Dispatchers.IO)

    suspend fun removerListaV4(listId: String, accessToken: String): Result<RemoveListResponse> {
        return safeApiCall { filmeAPIV4.removeList(accessToken, listId) }
    }


    // --- Métodos V3 (Mantidos por enquanto) ---

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


    // --- Helper para chamadas de API seguras ---

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Sucesso(body)
                } else {
                    Result.HttpError(response.code(), "Corpo da resposta vazio")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                Log.e("MinhaListaRepository", "Erro HTTP: ${response.code()} - $errorBody")
                Result.HttpError(response.code(), errorBody)
            }
        } catch (e: IOException) {
            Log.e("MinhaListaRepository", "Erro de Rede (IO): ${e.message}", e)
            Result.NetworkError("Falha na conexão. Verifique sua internet.")
        } catch (e: Exception) {
            Log.e("MinhaListaRepository", "Erro Desconhecido: ${e.message}", e)
            Result.UnknownError("Ocorreu um erro inesperado: ${e.message}")
        }
    }
}