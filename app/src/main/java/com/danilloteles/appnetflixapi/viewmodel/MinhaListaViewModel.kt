package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.filme.TmdbList
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MinhaListaViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val minhaListaRepository: MinhaListaRepository,
    private val filmeAPI: FilmeAPI
) : ViewModel() {

    private val _minhasListasState = MutableStateFlow<UiState<List<TmdbList>>>(UiState.Idle)
    val minhasListasState: StateFlow<UiState<List<TmdbList>>> = _minhasListasState

    init {
        buscarMinhasListas()
    }

    fun buscarMinhasListas() {
        viewModelScope.launch {
            _minhasListasState.value = UiState.Loading
            try {
                val sesssionId = userPreferencesRepository.sessionId.first()
                val accountId = userPreferencesRepository.accountId.first()

                if (sesssionId == null || accountId == null) {
                    _minhasListasState.value = UiState.Error("Usuário não autenticado.")
                    return@launch
                }

                val response = filmeAPI.obterListasDeContas(
                    accountId = accountId.toInt(),
                    sessionId = sesssionId
                )

                if (response.isSuccessful && response.body() != null) {
                    val listas = response.body()!!.results
                    _minhasListasState.value = UiState.Success(listas)
                } else {
                    val errorMsg = "Error ao buscar listas: ${response.code()} - ${response.message()}"
                    _minhasListasState.value = UiState.Error(errorMsg)
                    Log.e("TAG-MinhaListaViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Exceção ao buscar listas: ${e.message}"
                _minhasListasState.value = UiState.Error(errorMsg)
                Log.e("TAG-MinhaListaViewModel", errorMsg, e)
            }
        }
    }

    class MinhaListaViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val minhaListaRepository: MinhaListaRepository,
        private val filmeAPI: FilmeAPI
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MinhaListaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MinhaListaViewModel(
                    userPreferencesRepository,
                    minhaListaRepository,
                    filmeAPI
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}