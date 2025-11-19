package com.danilloteles.appnetflixapi.model

data class AccountListsResponse(
    val page: Int,
    val results: List<TmdbList>,
    val total_pages: Int,
    val total_results: Int
)
