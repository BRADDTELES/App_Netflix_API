package com.danilloteles.appnetflixapi.utils.events

sealed class FilmeListFilterState {
    object Popular : FilmeListFilterState()
    object TopRated : FilmeListFilterState()
    object NowPlaying : FilmeListFilterState()
}