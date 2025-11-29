package com.danilloteles.appnetflixapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.paging.serie.PopularSeriesPagingSource
import com.danilloteles.appnetflixapi.datasource.paging.serie.TopRatedSeriesPagingSource
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.repository.v3.SerieRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.SerieListFilterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest

class SerieViewModel(
    private val serieRepository: SerieRepository
) : ViewModel() {

    private val filmeAPI: FilmeAPI = RetrofitHelper.filmeAPI

    private val _currentFilter = MutableStateFlow<SerieListFilterState>(SerieListFilterState.Popular)
    val currentFilter: StateFlow<SerieListFilterState> = _currentFilter

    val seriesStream: Flow<PagingData<MediaItem>> = _currentFilter.flatMapLatest { filter ->
        createPagerForFilter(filter).flow
    }.cachedIn(viewModelScope)

    fun applyFilter(filter: SerieListFilterState) {
        _currentFilter.value = filter
    }

    private fun createPagerForFilter(filter: SerieListFilterState): Pager<Int, MediaItem> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                when (filter) {
                    SerieListFilterState.Popular -> PopularSeriesPagingSource(serieRepository.filmeAPI)
                    SerieListFilterState.TopRated -> TopRatedSeriesPagingSource(serieRepository.filmeAPI)
                }
            }
        )
    }

    class SerieListModelFactory(
        private val serieRepository: SerieRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SerieViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SerieViewModel(serieRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}