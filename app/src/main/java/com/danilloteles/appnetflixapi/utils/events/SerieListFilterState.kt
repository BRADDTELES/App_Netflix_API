package com.danilloteles.appnetflixapi.utils.events

sealed class SerieListFilterState {
    object Popular : SerieListFilterState()
    object TopRated : SerieListFilterState()
}