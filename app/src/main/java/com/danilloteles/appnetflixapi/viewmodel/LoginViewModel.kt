package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.filme.CreateSessionRequest
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.LoginEvent
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val filmeAPI: FilmeAPI = RetrofitHelper.filmeAPI

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent

    var requestToken: String? = null

    fun authenticateWithTmdb() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = filmeAPI.criarTokenDeSolicitacao()
                if (response.isSuccessful) {
                    response.body()?.let { tokenResponse ->
                        if (tokenResponse.success) {
                            requestToken = tokenResponse.request_token
                            Log.d("TAG-LoginViewModel","requestToken: ${requestToken}")
                            val authUrl = "https://www.themoviedb.org/authenticate/$requestToken?redirect_to=netflixapp://auth"
                            _loginEvent.emit(LoginEvent.OpenWebView(authUrl))
                            _uiState.value = UiState.Success(Unit)
                        } else {
                            _uiState.value = UiState.Error("Falha ap obter request token.")
                            Log.e(
                                "TAG-LoginViewModel",
                                "Falha ap obter request token: ${tokenResponse.request_token}"
                            )
                        }
                    } ?: run {
                        _uiState.value = UiState.Error("Resposta vazia ao obter request token.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro HTTP ao obter request token.")
                    Log.e("TAG-LoginViewModel", "Erro HTTP ao obter request token: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao obter request token.")
                Log.e("TAG-LoginViewModel", "Erro: ${e.message}", e)
            }
        }
    }

    fun createSession() {
        Log.d("TAG-LoginViewModel", "Iniciando createSession com requestToken: $requestToken")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            requestToken?.let { token ->
                try {
                    val sessionResponse = filmeAPI.criarIDdaSessão(CreateSessionRequest(request_token = token))
                    if (sessionResponse.isSuccessful && sessionResponse.body()?.success == true) {
                        val sessionId = sessionResponse.body()!!.session_id
                        Log.d("TAG-LoginViewModel", "Session ID criado com sucesso: $sessionId")

                        // Agora, obter detalhes da conta para pegar o accountId
                        val accountResponse = filmeAPI.obterDetalhesDaConta(sessionId)
                        if (accountResponse.isSuccessful && accountResponse.body() != null) {
                            val accountId = accountResponse.body()!!.id.toString()
                            Log.d("TAG-LoginViewModel", "Account ID obtido com sucesso: $accountId")

                            // Salvar ambos os IDs
                            userPreferencesRepository.saveSessionId(sessionId)
                            userPreferencesRepository.saveAccountId(accountId)

                            _loginEvent.emit(LoginEvent.LoginSuccess)
                            Log.d("TAG-LoginViewModel", "LoginEvent.LoginSuccess emitido.")
                            _uiState.value = UiState.Success(Unit)
                        } else {
                            _uiState.value = UiState.Error("Falha ao obter detalhes da conta.")
                            Log.e("TAG-LoginViewModel", "Falha ao obter detalhes da conta: ${accountResponse.code()}")
                        }
                    } else {
                        _uiState.value = UiState.Error("Falha ao criar session id.")
                        Log.e("TAG-LoginViewModel", "Falha ao criar sessão: ${sessionResponse.message()}")
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Erro de conexão ao criar session id")
                    Log.e("TAG-LoginViewModel", "Erro: ${e.message}", e)
                }
            } ?: run {
                _uiState.value =
                    UiState.Error("Request token não disponível. Favor tentar novamente.")
                Log.e("LoginViewModel", "Request token não disponível. Favor tentar novamente.")
            }
        }
    }

    class LoginViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown View Model class")
        }
    }
}