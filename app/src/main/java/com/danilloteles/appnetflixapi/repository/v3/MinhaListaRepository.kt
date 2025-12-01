/*
package com.danilloteles.appnetflixapi.repository.v3

import android.util.Log
import com.danilloteles.appnetflixapi.api.APIV4
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v3.ListaDetalhesResposta
import com.danilloteles.appnetflixapi.model.v3.filme.AccountDetailsResponse
import com.danilloteles.appnetflixapi.model.v3.filme.AccountListsResponse
import com.danilloteles.appnetflixapi.model.v3.filme.AddRemoveListItemRequest
import com.danilloteles.appnetflixapi.model.v3.filme.CreateListRequest
import com.danilloteles.appnetflixapi.model.v3.filme.CreateListResponse
import com.danilloteles.appnetflixapi.model.v3.filme.ListaItemResposta
import com.danilloteles.appnetflixapi.model.v3.filme.ListaResposta
import com.danilloteles.appnetflixapi.model.v4.response.AccountListsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RemoveListV4Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.io.IOException

class MinhaListaRepository(
    private val filmeAPI: FilmeAPI,
    private val APIV4: APIV4, // Injetando a API v4
    private val userPreferencesRepository: UserPreferencesRepository
) {

    // --- Métodos V4 ---

    fun obterListasDaContaV4(accountId: String, accessToken: String): Flow<Result<AccountListsV4Response>> = flow {
        emit(safeApiCall { APIV4.getAccountLists(accessToken, accountId) })
    }.flowOn(Dispatchers.IO)

    suspend fun removerListaV4(listId: String, accessToken: String): Result<RemoveListV4Response> {
        return safeApiCall { APIV4.removeList(accessToken, listId) }
    }


    // --- Métodos V3 ---

    suspend fun obterDetalhesDaConta(sessionId: String): Response<AccountDetailsResponse> {
        return filmeAPI.obterDetalhesDaConta(sessionId)
    }

    suspend fun criarLista(sessionId: String, request: CreateListRequest): Response<CreateListResponse> {
        return filmeAPI.criarLista(sessionId, request)
    }

    suspend fun obterListasDaConta(accountId: Int, sessionId: String): Response<AccountListsResponse> {
        return filmeAPI.obterListasDaConta(accountId, sessionId)
    }

    suspend fun obterDetalhesDaLista(listId: String, sessionId: String): Response<ListaDetalhesResposta> {
        return filmeAPI.obterDetalhesDaLista(listId, sessionId)
    }

    suspend fun adicionarItemALista(listId: String, sessionId: String, request: AddRemoveListItemRequest): Response<ListaItemResposta> {
        return filmeAPI.adicionarItemALista(listId, sessionId, request)
    }

    suspend fun removerItemDaLista(listId: String, sessionId: String, request: AddRemoveListItemRequest): Response<ListaItemResposta> {
        return filmeAPI.removerItemDaLista(listId, sessionId, request)
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
}*/
