package com.danilloteles.appnetflixapi.constantes

object AppDestinations {
    const val SPLASH_SCREEN = "splash"
    const val MAIN_SCREEN = "main"
    const val MOVIE_FORM_SCREEN = "movie_form"
    const val MOVIE_DETAILS_SCREEN = "movie_details"
    const val MOVIE_ID_ARG = "movieId"

    val MOVIE_DETAILS_ROUTE = "$MOVIE_DETAILS_SCREEN/{$MOVIE_ID_ARG}"
    val MOVIE_FORM_ROUTE = "$MOVIE_FORM_SCREEN/{$MOVIE_ID_ARG}"
}