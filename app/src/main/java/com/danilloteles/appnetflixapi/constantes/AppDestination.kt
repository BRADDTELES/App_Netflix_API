package com.danilloteles.appnetflixapi.constantes

object AppDestination {
    const val SPLASH_SCREEN = "splash"
    const val MAIN_SCREEN = "main"
    const val LIST_FORM_SCREEN = "list_form"
    const val MOVIE_DETAILS_SCREEN = "movie_details"
    const val LOGIN_SCREEN = "login"
    const val MINHA_LISTA_SCREEN = "minha_lista"
    const val CONTEUDO_SCREEN = "conteudo"
    const val SERIES_LIST_SCREEN = "series_list"
    const val FILMES_LIST_SCREEN = "filmes_list"
    const val MY_MOVIE_DETAILS = "my_movie_details"
    const val MY_SERIES_DETAILS = "my_serie_details"

    const val MOVIE_ID_ARG = "movieId"
    const val SERIE_ID_ARG = "serieId"
    const val REQUEST_TOKEN_ARG = "request_token"
    const val LIST_ID_ARG = "listId"
    const val MOVIE_TITLE_ARG = "movieTitle" // Novo argumento para o título
    const val DESTINATION_ARG = "destination" // Argumento para o destino pós-login

    val MOVIE_DETAILS_ROUTE = "$MOVIE_DETAILS_SCREEN/{$MOVIE_ID_ARG}"
    val CONTEUDO_ROUTE = "$CONTEUDO_SCREEN/{$LIST_ID_ARG}/{$MOVIE_TITLE_ARG}" // Nova rota para ConteudoScreen
    val LIST_FORM_ROUTE = LIST_FORM_SCREEN
    const val LOGIN_ROUTE_PATTERN = "$LOGIN_SCREEN?${DESTINATION_ARG}={${DESTINATION_ARG}}&${REQUEST_TOKEN_ARG}={${REQUEST_TOKEN_ARG}}"
    val MY_MOVIE_DETAILS_ROUTE = "$MY_MOVIE_DETAILS/{$MOVIE_ID_ARG}?$LIST_ID_ARG={$LIST_ID_ARG}"
    val MY_SERIES_DETAILS_ROUTE = "$MY_SERIES_DETAILS/{$SERIE_ID_ARG}?$LIST_ID_ARG={$LIST_ID_ARG}"
}