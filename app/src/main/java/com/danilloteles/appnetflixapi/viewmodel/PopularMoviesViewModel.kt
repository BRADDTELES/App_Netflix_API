package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.repository.FilmeRepository
import kotlinx.coroutines.flow.Flow

class PopularMoviesViewModel(
    private val repository: FilmeRepository
) : ViewModel() {

    val popularMoviesStream: Flow<PagingData<Filme>> = repository
        .getPopularMoviesStream()
        .cachedIn(viewModelScope)

}
