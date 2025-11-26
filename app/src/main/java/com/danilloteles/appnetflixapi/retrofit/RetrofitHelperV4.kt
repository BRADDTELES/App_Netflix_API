package com.danilloteles.appnetflixapi.retrofit

import com.danilloteles.appnetflixapi.BuildConfig
import com.danilloteles.appnetflixapi.api.FilmeAPIV4
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelperV4 {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.API_READ_ACCESS_TOKEN}")
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .build()

            chain.proceed(newRequest)
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val filmeApiV4: FilmeAPIV4 = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/4/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FilmeAPIV4::class.java)
}