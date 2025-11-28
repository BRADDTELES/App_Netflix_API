package com.danilloteles.appnetflixapi.repository.v4

import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.v4.request.AccessTokenV4Request
import com.danilloteles.appnetflixapi.model.v4.request.AddItemsV4Request
import com.danilloteles.appnetflixapi.model.v4.request.CreateListV4Request
import com.danilloteles.appnetflixapi.model.v4.request.MediaItemV4Request
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsV4Request
import com.danilloteles.appnetflixapi.model.v4.request.RequestTokenV4Request
import com.danilloteles.appnetflixapi.model.v4.response.AccessTokenV4Response
import com.danilloteles.appnetflixapi.model.v4.response.AccountListsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.AddItemsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.CreateListV4Response
import com.danilloteles.appnetflixapi.model.v4.response.ListDetailsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RemoveItemsV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RemoveListV4Response
import com.danilloteles.appnetflixapi.model.v4.response.RequestTokenV4Response
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelperV4
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RepositoryV4(
    private val userPreferences: UserPreferencesRepository? = null // opcional injeção
) {

    private val apiV4 = RetrofitHelperV4.apiV4
    private val authApiV4 = RetrofitHelperV4.authApiV4

    suspend fun createRequestToken(redirectTo: String?): Result<RequestTokenV4Response> =
        safeApiCall {
            authApiV4.createRequestToken(
                RequestTokenV4Request(redirect_to = redirectTo ?: "")
            )
        }

    suspend fun createAccessToken(requestToken: String): Result<AccessTokenV4Response> =
        safeApiCall {
            authApiV4.createAccessToken(
                AccessTokenV4Request(request_token = requestToken)
            )
        }

    suspend fun createList(accessToken: String, nome: String, descricao: String?): Result<CreateListV4Response> =
        safeApiCall {
            apiV4.createList(
                "Bearer $accessToken",
                CreateListV4Request(name = nome, description = descricao)
            )
        }

    suspend fun getAccountLits(accessToken: String, accountObjectId: String): Result<Response<AccountListsV4Response>> =
        safeApiCall {
            apiV4.getAccountLists(
                "Bearer $accessToken",
                accountObjectId
            )
        }

    suspend fun getListDetails(accessToken: String, listId: String): Result<ListDetailsV4Response> =
        safeApiCall {
            apiV4.getListDetails(
                "Bearer $accessToken",
                listId
            )
        }

    suspend fun removeList(accessToken: String, listId: String): Result<Response<RemoveListV4Response>> =
        safeApiCall {
            apiV4.removeList(
                "Bearer $accessToken",
                listId
            )
        }

    suspend fun addMovie(accessToken: String, listId: String, movieId: Int): Result<AddItemsV4Response> =
        safeApiCall {
            apiV4.addItems(
                "Bearer $accessToken",
                listId,
                AddItemsV4Request(listOf(MediaItemV4Request("movie", movieId)))
            )
        }

    suspend fun addSerie(accessToken: String, listId: String, tvId: Int): Result<AddItemsV4Response> =
        safeApiCall {
            apiV4.addItems(
                "Bearer $accessToken",
                listId,
                AddItemsV4Request(listOf(MediaItemV4Request("tv", tvId)))
            )
        }

    suspend fun removeMovie(accessToken: String, listId: String, movieId: Int): Result<RemoveItemsV4Response> =
        safeApiCall {
            apiV4.removeItems(
                "Bearer $accessToken",
                listId,
                RemoveItemsV4Request(listOf(MediaItemV4Request("movie", movieId)))
            )
        }

    suspend fun removeSerie(accessToken: String, listId: String, tvId: Int): Result<RemoveItemsV4Response> =
        safeApiCall {
            apiV4.removeItems(
                "Bearer $accessToken",
                listId,
                RemoveItemsV4Request(listOf(MediaItemV4Request("tv", tvId)))
            )
        }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
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