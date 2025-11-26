package com.danilloteles.appnetflixapi.common

sealed class Result<out T> {
    data class Sucesso<T>(val data: T) : Result<T>()
    data class HttpError(val code: Int, val mensagem: String) : Result<Nothing>()
    data class NetworkError(val mensagem: String = "Erro de conexão") : Result<Nothing>()
    data class UnknownError(val mensagem: String = "Erro inesperado") : Result<Nothing>()
}