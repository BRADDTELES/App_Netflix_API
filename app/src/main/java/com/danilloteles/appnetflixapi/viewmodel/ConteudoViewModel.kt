package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.model.ListaDetalhesResposta
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.model.filme.toMediaItem
import com.danilloteles.appnetflixapi.model.serie.toMediaItem
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConteudoViewModel(
    private val listId: String,
    private val minhaListaRepository: MinhaListaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _conteudoState = MutableStateFlow<UiState<ListaDetalhesResposta>>(UiState.Idle)
    val conteudoState: StateFlow<UiState<ListaDetalhesResposta>> = _conteudoState

    init {
        buscarConteudo()
    }

    fun buscarConteudo() {
        viewModelScope.launch {
            _conteudoState.value = UiState.Loading
            try {
                val sessionId = userPreferencesRepository.sessionId.first()
                if (sessionId == null) {
                    _conteudoState.value = UiState.Error("Usuário não autenticado.")
                    return@launch
                }

                val response = minhaListaRepository.obterDetalhesDaLista(listId, sessionId)
                if (response.isSuccessful && response.body() != null) {

                    val initialResponseData = response.body()!!
                    val itemsFromList = initialResponseData.items
                    Log.d("ConteudoViewModel", "Lista inicial recebida com ${itemsFromList.size} itens.")
                    val correctItems = mutableListOf<MediaItem>()

                    for (item in itemsFromList) {
                        Log.d("ConteudoViewModel", "Processando item com ID: ${item.id} e media_type reportado: ${item.media_type}")
                        try {
                            // Tenta buscar como FILME primeiro
                            val filmeResponse = minhaListaRepository.filmeAPI.recuperarDetalhesFilme(item.id)
                            if (filmeResponse.isSuccessful && filmeResponse.body() != null) {
                                correctItems.add(filmeResponse.body()!!.toMediaItem())
                                Log.d("ConteudoViewModel", "   ... SUCESSO como FILME: '${filmeResponse.body()!!.title}'")
                            } else {
                                // Se falhar, tenta buscar como SÉRIE
                                val serieResponse = minhaListaRepository.filmeAPI.recuperarDetalhesSerie(item.id)
                                if (serieResponse.isSuccessful && serieResponse.body() != null) {
                                    correctItems.add(serieResponse.body()!!.toMediaItem())
                                    Log.d("ConteudoViewModel", "   ... SUCESSO como SÉRIE: '${serieResponse.body()!!.name}'")
                                } else {
                                    Log.w("ConteudoViewModel", "   ... FALHA como filme ou série. Desistindo do ID ${item.id}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ConteudoViewModel", "Exceção ao buscar detalhes para o ID ${item.id}: ${e.message}", e)
                        }
                    }

                    val correctedResponse = initialResponseData.copy(items = correctItems)
                    _conteudoState.value = UiState.Success(correctedResponse)
                    Log.d("ConteudoViewModel", "Processamento concluído. Retornando ${correctItems.size} itens corrigidos.")

                } else {
                    val errorMsg = "Erro ao buscar conteúdo da lista: ${response.code()} - ${response.message()}"
                    _conteudoState.value = UiState.Error(errorMsg)
                    Log.e("TAG-ConteudoViewModel", errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Exceção ao buscar conteúdo da lista: ${e.message}"
                _conteudoState.value = UiState.Error(errorMsg)
                Log.e("TAG-ConteudoViewModel", errorMsg, e)
            }
        }
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
