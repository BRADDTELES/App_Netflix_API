package com.danilloteles.appnetflixapi.utils.events

sealed class MovieListFilterState {
    object Popular : MovieListFilterState()
    object TopRated : MovieListFilterState()
    object NowPlaying : MovieListFilterState()
    data class MyList(val listId: String?) : MovieListFilterState()
}