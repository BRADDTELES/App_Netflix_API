package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danilloteles.appnetflixapi.datasource.paging.filme.PopularFilmesPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.filme.TopRatedFilmesPagingSource
import com.danilloteles.appnetflixapi.model.filme.Filme
import com.danilloteles.appnetflixapi.repository.FilmeRepository
import com.danilloteles.appnetflixapi.utils.events.FilmeListFilterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

class FilmeViewModel(
    private val filmeRepository: FilmeRepository
) : ViewModel() {

    private val _currentFilter = MutableStateFlow<FilmeListFilterState>(FilmeListFilterState.Popular)
    val currentFilter: StateFlow<FilmeListFilterState> = _currentFilter

    val filmesStream: Flow<PagingData<Filme>> = _currentFilter.flatMapLatest { filter ->
        createPagerForFilter(filter).flow
    }.cachedIn(viewModelScope)

    private fun createPagerForFilter(filter: FilmeListFilterState): Pager<Int, Filme> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                when (filter) {
                    FilmeListFilterState.Popular -> PopularFilmesPagingSource(filmeRepository.filmeAPI)
                    FilmeListFilterState.TopRated -> TopRatedFilmesPagingSource(filmeRepository.filmeAPI)
                }
            }
        )
    }

    fun applyFilter(filter: FilmeListFilterState) {
        _currentFilter.value = filter
    }

    class FilmeListModelFactory(
        private val filmeRepository: FilmeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilmeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FilmeViewModel(filmeRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}