package com.danilloteles.appnetflixapi.model.filme

data class AccountDetailsResponse(
    val avatar: Avatar,
    val id: Int,
    val iso_639_1: String,
    val iso_3166_1: String,
    val name: String,
    val include_adult: Boolean,
    val username: String
)
