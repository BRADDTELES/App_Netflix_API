package com.danilloteles.appnetflixapi.utils.events

enum class SortOrder {
    DEFAULT,
    TITLE_ASC // A-Z
}

sealed class MovieListFilterState {
    data class MyList(
        val listId: String?,
        val sortOrder: SortOrder = SortOrder.DEFAULT
    ) : MovieListFilterState()
}
