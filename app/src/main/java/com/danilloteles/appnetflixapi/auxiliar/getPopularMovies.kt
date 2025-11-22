package com.danilloteles.appnetflixapi.auxiliar

import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.model.filme.FilmeFicticio

fun getPopularMovies(): List<FilmeFicticio> {
    return listOf(
        FilmeFicticio(1, "Matrix", "Descrição do filme", R.drawable.movie_matrix),
        FilmeFicticio(2, "Show de Vizinha","Descrição do filme", R.drawable.movie_show_vizinha),
        FilmeFicticio(3, "Interstellar","Descrição do filme", R.drawable.movie_interstellar),
        FilmeFicticio(4, "The Dark Knight","Descrição do filme", R.drawable.movie_dark_knight),
        FilmeFicticio(5, "Pulp Fiction","Descrição do filme", R.drawable.movie_pulp_fiction),
        FilmeFicticio(6, "Fight Club","Descrição do filme", R.drawable.movie_fight_club),
        FilmeFicticio(7, "Star Wars: O Despertar da Força","Descrição do filme", R.drawable.movie_star_war),
        FilmeFicticio(8, "Todo Mundo em Pânico","Descrição do filme", R.drawable.movie_todo_mundo_panico),
        FilmeFicticio(9, "Liga da Justiça","Descrição do filme", R.drawable.movie_liga_da_justica),
        FilmeFicticio(10, "O Senhor dos Anéis","Descrição do filme", R.drawable.movie_senhor_dos_aneis),
    )
}