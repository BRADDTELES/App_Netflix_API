package com.danilloteles.appnetflixapi.model.filme

data class CreateListRequest(
    val name: String,
    val description: String,
    val iso_639_1: String = "en"
)
