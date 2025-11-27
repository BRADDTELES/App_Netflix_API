package com.danilloteles.appnetflixapi.retrofit

import com.danilloteles.appnetflixapi.BuildConfig
import com.danilloteles.appnetflixapi.api.FilmeAPIV4
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelperV4 {

    private const val BASE_URL = "https://api.themoviedb.org/4/"

    // Cliente OkHttp SEM o interceptor de autorização
    // Usado para chamadas onde o Access Token do usuário será injetado dinamicamente
    private val publicOkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .addHeader("Accept","application/json")
                .build()
            chain.proceed(newRequest)
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Cliente OkHttp COM o interceptor que adiciona o Read Access Token (v4)
    // Usado para o fluxo inicial de autenticação (/auth/request_token e /auth/access_token)
    private val authOkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.API_READ_ACCESS_TOKEN}")
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .addHeader("Accept","application/json")
                .build()

            chain.proceed(newRequest)
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()


    // Instância do Retrofit para chamadas que precisam do Access Token do USUÁRIO
    val filmeApiV4: FilmeAPIV4 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(publicOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FilmeAPIV4::class.java)
    }

    // Instância do Retrofit para o fluxo de AUTENTICAÇÃO (usa o Read Access Token)
    val authApiV4: FilmeAPIV4 by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FilmeAPIV4::class.java)
    }
}