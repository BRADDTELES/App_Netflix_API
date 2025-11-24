package com.danilloteles.appnetflixapi.retrofit

import com.danilloteles.appnetflixapi.BuildConfig
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.constantes.Constantes
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper {
    companion object {

        private const val API_KEY_PARAM = "api_key"

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.newBuilder()
                    .addQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                    .build()
                val newRequest = request.newBuilder()
                    .url(url)
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        val filmeAPI: FilmeAPI = Retrofit.Builder()
            .baseUrl(Constantes.BASE_URL_V3)
            .addConverterFactory(GsonConverterFactory.create() )
            .client(okHttpClient)
            .build()
            .create( FilmeAPI::class.java )
    }
}