package com.danilloteles.appnetflixapi.constantes

object AppDestinations {
    const val SPLASH_SCREEN = "splash"
    const val MAIN_SCREEN = "main"
    const val LIST_FORM_SCREEN = "list_form"
    const val MOVIE_DETAILS_SCREEN = "movie_details"
    const val LOGIN_SCREEN = "login"
    const val MY_LIST_SCREEN = "my_list"
    const val MY_MOVIE_DETAILS = "my_movie_details"

    const val MOVIE_ID_ARG = "movieId"
    const val REQUEST_TOKEN_ARG = "request_token"

    val MOVIE_DETAILS_ROUTE = "$MOVIE_DETAILS_SCREEN/{$MOVIE_ID_ARG}"
    val LIST_FORM_ROUTE = LIST_FORM_SCREEN
    val LOGIN_ROUTE = "$LOGIN_SCREEN?$REQUEST_TOKEN_ARG={$REQUEST_TOKEN_ARG}"
}