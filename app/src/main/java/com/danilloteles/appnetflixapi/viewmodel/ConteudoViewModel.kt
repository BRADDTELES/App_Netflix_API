package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.utils.events.MovieListFilterState
import com.danilloteles.appnetflixapi.utils.events.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

class ConteudoViewModel(
    listId: String,
    private val minhaListaRepository: MinhaListaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(MovieListFilterState.MyList(listId = listId))
    val filterState: StateFlow<MovieListFilterState.MyList> = _filterState

    @OptIn(ExperimentalCoroutinesApi::class)
    val conteudoPaginado: Flow<PagingData<MediaItem>> = _filterState.flatMapLatest { state ->
        minhaListaRepository.getMyListMoviesStream(state.listId, state.sortOrder)
    }.cachedIn(viewModelScope)

    fun setSortOrder(sortOrder: SortOrder) {
        _filterState.value = _filterState.value.copy(sortOrder = sortOrder)
    }

    class ConteudoViewModelFactory(
        private val listId: String,
        private val minhaListaRepository: MinhaListaRepository,
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConteudoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConteudoViewModel(listId, minhaListaRepository, userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}