package com.danilloteles.appnetflixapi.repository.v4

import androidx.compose.foundation.text.input.delete
import androidx.core.graphics.get
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.ktor.KtorClientV4
import com.danilloteles.appnetflixapi.model.v4.request.AccessTokenRequest
import com.danilloteles.appnetflixapi.model.v4.request.AddItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.CreateListRequest
import com.danilloteles.appnetflixapi.model.v4.request.MediaItemRequest
import com.danilloteles.appnetflixapi.model.v4.request.RemoveItemsRequest
import com.danilloteles.appnetflixapi.model.v4.request.RequestTokenRequest
import com.danilloteles.appnetflixapi.model.v4.response.AccessTokenResponse
import com.danilloteles.appnetflixapi.model.v4.response.AddItemsResponse
import com.danilloteles.appnetflixapi.model.v4.response.CreateListResponse
import com.danilloteles.appnetflixapi.model.v4.response.ListDetailsResponse
import com.danilloteles.appnetflixapi.model.v4.response.RemoveItemsResponse
import com.danilloteles.appnetflixapi.model.v4.response.RequestTokenResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import java.net.ConnectException

class FilmeRepositoryV4Ktor {

    private val authClient = KtorClientV4.authClient
    private val publicClient = KtorClientV4.publicClient

    suspend fun createRequestToken(redirectTo: String?): Result<RequestTokenResponse> =
        safeApiCall {
            authClient.post("auth/request_token") {
                setBody(
                    RequestTokenRequest(
                        redirect_to = redirectTo ?: "http://www.themoviedb.org/"
                    )
                )
            }.body()
        }

    suspend fun createAccessToken(requestToken: String): Result<AccessTokenResponse> =
        safeApiCall {
            authClient.post("auth/access_token") {
                setBody(AccessTokenRequest(request_token = requestToken))
            }.body()
        }

    suspend fun createList(accessToken: String, nome: String, descricao: String?): Result<CreateListResponse> =
        safeApiCall {
            publicClient.post("list") {
                header("Authorization", "Bearer $accessToken")
                setBody(CreateListRequest(name = nome, description = descricao))
            }.body()
        }

    // TODO: Implementar os outros métodos (addMovie, getListDetails, removeMovie...) de forma similar.
    suspend fun addMovie(accessToken: String, listId: String, movieId: Int): Result<AddItemsResponse> =
        safeApiCall {
            val requestBody = AddItemsRequest(
                items = listOf(MediaItemRequest(media_type = "movie", media_id = movieId))
            )
            publicClient.post("list/$listId/items") {
                header("Authorization", "Bearer $accessToken")
                setBody(requestBody)
            }.body()
        }

    suspend fun addSerie(accessToken: String, listId: String, tvId: Int): Result<AddItemsResponse> =
        safeApiCall {
            val requestBody = AddItemsRequest(
                items = listOf(MediaItemRequest(media_type = "tv", media_id = tvId))
            )
            publicClient.post("list/$listId/items") {
                header("Authorization", "Bearer $accessToken")
                setBody(requestBody)
            }.body()
        }

    suspend fun removeMovie(accessToken: String, listId: String, movieId: Int): Result<RemoveItemsResponse> =
        safeApiCall {
            val requestBody = RemoveItemsRequest(
                items = listOf(
                    MediaItemRequest(media_type = "movie", media_id = movieId)
                )
            )
            publicClient.delete("list/$listId/items") {
                header("Authorization", "Bearer $accessToken")
                setBody(requestBody)
            }.body()
        }

    suspend fun removeSerie(accessToken: String, listId: String, tvId: Int): Result<RemoveItemsResponse> =
        safeApiCall {
            val requestBody = RemoveItemsRequest(
                items = listOf(
                    MediaItemRequest(media_type = "tv", media_id = tvId)
                )
            )
            publicClient.delete("list/$listId/items") {
                header("Authorization", "Bearer $accessToken")
                setBody(requestBody)
            }.body()
        }

    /**
     * Busca os detalhes de uma lista específica, incluindo os filmes contidos nela.
     */
    suspend fun getListDetails(accessToken: String, listId: String): Result<ListDetailsResponse> =
        safeApiCall {
            publicClient.get("list/$listId") {
                header("Authorization", "Bearer $accessToken")
                // Parâmetros de query, como 'language', podem ser adicionados aqui se necessário
                parameter("language", "pt-BR")
            }.body()
        }

    /**
     * Remove todos os itens de uma lista.
     */
    suspend fun clearList(accessToken: String, listId: Int): Result<RemoveItemsResponse> =
        safeApiCall {
            // Este endpoint da API v4 é um GET, o que também é incomum para uma ação de modificação.
            publicClient.get("list/$listId/clear") {
                header("Authorization", "Bearer $accessToken")
            }.body()
        }

    /**
     * Deleta uma lista inteira.
     */
    suspend fun deleteList(accessToken: String, listId: Int): Result<RemoveItemsResponse> =
        safeApiCall {
            publicClient.delete("list/$listId") {
                header("Authorization", "Bearer $accessToken")
            }.body()
        }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Sucesso(apiCall())
        } catch (e: RedirectResponseException) { // Qualquer Status --> 3xx
            class FilmeRepositoryV4Ktor {

                private val authClient = KtorClientV4.authClient
                private val publicClient = KtorClientV4.publicClient

                //...
            }
            Result.HttpError(e.response.status.value, e.response.bodyAsText())
        } catch (e: ClientRequestException) { // Qualquer Status --> 4xx
            Result.HttpError(e.response.status.value, e.response.bodyAsText())
        } catch (e: ServerResponseException) { // Qualquer Status --> 5xx
            Result.HttpError(e.response.status.value, e.response.bodyAsText())
        } catch (e: ConnectException) {
            Result.NetworkError("Sem conexão com a internet.")
        } catch (e: Exception) {
            Result.UnknownError(e.message ?: "Erro desconhecido")
        }
    }
}