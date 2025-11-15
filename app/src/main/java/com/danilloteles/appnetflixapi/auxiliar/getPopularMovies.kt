package com.danilloteles.appnetflixapi.auxiliar

import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.model.Movie

fun getPopularMovies(): List<Movie> {
    return listOf(
        Movie(1, "Matrix", R.drawable.movie_matrix),
        Movie(2, "Inception", R.drawable.movie_inception),
        Movie(3, "Interstellar", R.drawable.movie_interstellar),
        Movie(4, "The Dark Knight", R.drawable.movie_dark_knight),
        Movie(5, "Pulp Fiction", R.drawable.movie_pulp_fiction),
        Movie(6, "Fight Club", R.drawable.movie_fight_club),
        Movie(7, "Star Wars: O Despertar da Força", R.drawable.movie_star_war),
        Movie(8, "Todo Mundo em Pânico", R.drawable.movie_todo_mundo_panico),
        Movie(9, "Liga da Justiça", R.drawable.movie_liga_da_justica),
        Movie(10, "O Senhor dos Anéis", R.drawable.movie_senhor_dos_aneis),
    )
}