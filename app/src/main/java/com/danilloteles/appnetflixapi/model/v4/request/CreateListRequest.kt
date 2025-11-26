package com.danilloteles.appnetflixapi.model.v4.request

data class CreateListRequest(
    val name: String,
    val description: String?,
    val iso_639_1: String = "pt"
)
