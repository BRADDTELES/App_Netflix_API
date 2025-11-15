package com.danilloteles.appnetflixapi.retrofit

import com.danilloteles.appnetflixapi.api.FilmeAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHelper {
    companion object {
        val filmeAPI = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create() )
            .build()
            .create( FilmeAPI::class.java )
    }
}