package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.repository.v3.FilmeRepository
import kotlinx.coroutines.flow.Flow

class PopularFilmeViewModel(
    private val repository: FilmeRepository
) : ViewModel() {

    val popularMoviesStream: Flow<PagingData<MediaItem>> = repository
        .getPopularMoviesStream()
        .cachedIn(viewModelScope)


    class PopularMoviesViewModelFactory(
        private val repository: FilmeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PopularFilmeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PopularFilmeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
