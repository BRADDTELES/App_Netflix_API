package com.danilloteles.appnetflixapi.ktor

import com.danilloteles.appnetflixapi.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientV4 {

    private const val BASE_URL = "https://api.themoviedb.org/4/"

    // Cliente Ktor para o fluxo de AUTENTICAÇÃO (usa o Read Access Token)
    val authClient = HttpClient(CIO) {
        defaultRequest {
            url(BASE_URL)
            header("Authorization", "Bearer ${BuildConfig.API_READ_ACCESS_TOKEN}")
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            level = LogLevel.ALL
        }
    }

    // Cliente Ktor para chamadas públicas que precisam do Access Token do USUÁRIO
    val publicClient = HttpClient(CIO) {
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

}