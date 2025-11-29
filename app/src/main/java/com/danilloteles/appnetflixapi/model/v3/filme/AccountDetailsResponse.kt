package com.danilloteles.appnetflixapi.model.v3.filme

data class AccountDetailsResponse(
    val avatar: Avatar,
    val id: Int,
    val iso_639_1: String,
    val iso_3166_1: String,
    val name: String,
    val include_adult: Boolean,
    val username: String
)
