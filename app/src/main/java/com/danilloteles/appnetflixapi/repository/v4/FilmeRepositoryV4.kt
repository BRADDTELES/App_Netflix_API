package com.danilloteles.appnetflixapi.repository.v4

import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.request.AccessTokenRequest
import com.danilloteles.appnetflixapi.model.v4.request.AddItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.CreateListRequest
import com.danilloteles.appnetflixapi.model.v4.request.MediaItemRequest
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.RequestTokenRequest
import com.danilloteles.appnetflixapi.model.v4.response.AccessTokenResponse
import com.danilloteles.appnetflixapi.model.v4.response.RequestTokenResponse
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelperV4
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FilmeRepositoryV4(
    private val userPreferences: UserPreferencesRepository? = null // opcional injeção
) {

    private val apiV4 = RetrofitHelperV4.filmeApiV4

    suspend fun createRequestToken(redirectTo: String): Result<RequestTokenResponse> =
        safeApiCall {
            apiV4.createRequestToken(RequestTokenRequest(redirect_to = redirectTo))
        }

    suspend fun createAccessToken(requestToken: String): Result<AccessTokenResponse> =
        safeApiCall {
            apiV4.createAccessToken(AccessTokenRequest(request_token = requestToken))
        }

    suspend fun createList(accessToken: String, nome: String, descricao: String?) =
        safeApiCall {
            apiV4.createList("Bearer $accessToken", CreateListRequest(name = nome, description = descricao))
        }

    suspend fun addMovie(accessToken: String, listId: String, movieId: Int) =
        safeApiCall {
            apiV4.addItems(
                "Bearer $accessToken",
                listId,
                AddItemsRequest(listOf(MediaItemRequest("movie", movieId)))
            )
        }

    suspend fun addSerie(accessToken: String, listId: String, tvId: Int) =
        safeApiCall {
            apiV4.addItems(
                "Bearer $accessToken",
                listId,
                AddItemsRequest(listOf(MediaItemRequest("tv", tvId)))
            )
        }

    suspend fun removeMovie(accessToken: String, listId: String, movieId: Int) =
        safeApiCall {
            apiV4.removeItems(
                "Bearer $accessToken",
                listId,
                RemoveItemsRequest(listOf(MediaItemRequest("movie", movieId)))
            )
        }

    suspend fun removeSerie(accessToken: String, listId: String, tvId: Int) =
        safeApiCall {
            apiV4.removeItems(
                "Bearer $accessToken",
                listId,
                RemoveItemsRequest(listOf(MediaItemRequest("tv", tvId)))
            )
        }

    suspend fun getListDetails(accessToken: String, listId: String) =
        safeApiCall {
            apiV4.getListDetails("Bearer $accessToken", listId)
        }

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Sucesso(apiCall())
        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            Result.HttpError(code, errorBody ?: e.message())
        } catch (e: SocketTimeoutException) {
            Result.NetworkError("Tempo de conexão esgotado")
        } catch (e: UnknownHostException) {
            Result.NetworkError("Sem conexão com a internet")
        } catch (e: Exception) {
            Result.UnknownError(e.message ?: "Erro desconhecido")
        }
    }
}