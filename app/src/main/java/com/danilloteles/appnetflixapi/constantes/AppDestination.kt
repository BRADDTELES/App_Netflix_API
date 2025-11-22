package com.danilloteles.appnetflixapi.constantes

object AppDestination {
    const val SPLASH_SCREEN = "splash"
    const val MAIN_SCREEN = "main"
    const val LIST_FORM_SCREEN = "list_form"
    const val MOVIE_DETAILS_SCREEN = "movie_details"
    const val SERIE_DETAILS_SCREEN = "serie_details"
    const val LOGIN_SCREEN = "login"
    const val MY_LIST_SCREEN = "my_list"
    const val SERIES_LIST_SCREEN = "series_list"
    const val FILMES_LIST_SCREEN = "filmes_list"
    const val MY_MOVIE_DETAILS = "my_movie_details"
    const val MY_SERIES_DETAILS = "my_serie_details"

    const val MOVIE_ID_ARG = "movieId"
    const val SERIE_ID_ARG = "serieId"
    const val REQUEST_TOKEN_ARG = "request_token"
    const val LIST_ID_ARG = "listId" // Novo argumento para o ID da lista

    val MOVIE_DETAILS_ROUTE = "$MOVIE_DETAILS_SCREEN/{$MOVIE_ID_ARG}"
    val SERIE_DETAILS_ROUTE = "$SERIE_DETAILS_SCREEN/{$SERIE_ID_ARG}"
    val LIST_FORM_ROUTE = LIST_FORM_SCREEN
    val LOGIN_ROUTE = "$LOGIN_SCREEN?$REQUEST_TOKEN_ARG={$REQUEST_TOKEN_ARG}"
    val MY_MOVIE_DETAILS_ROUTE = "$MY_MOVIE_DETAILS/{$MOVIE_ID_ARG}?$LIST_ID_ARG={$LIST_ID_ARG}"
    val MY_SERIES_DETAILS_ROUTE = "$MY_SERIES_DETAILS/{$SERIE_ID_ARG}?$LIST_ID_ARG={$LIST_ID_ARG}"
    val SERIES_LIST_ROUTE = SERIES_LIST_SCREEN
    val FILMES_LIST_ROUTE = FILMES_LIST_SCREEN
}