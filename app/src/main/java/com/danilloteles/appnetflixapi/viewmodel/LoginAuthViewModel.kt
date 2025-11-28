package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import com.danilloteles.appnetflixapi.utils.events.LoginEvent
import com.danilloteles.appnetflixapi.utils.events.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginAuthViewModel(
    private val repository: RepositoryV4,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling

    var currentRequestToken: String? = null
    private var pollingJob: kotlinx.coroutines.Job? = null

    // Passo 1: Criar request token e abrir navegador
    fun startAuthenticationV4() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = repository.createRequestToken("netflixapp://auth")) {
                is Result.Sucesso -> {
                    val requestToken = result.data.request_token
                    currentRequestToken = requestToken

                    Log.d("AuthV4ViewModel", "Request Token criado: $requestToken")

                    // URL correta para v4
                    val authUrl = "https://www.themoviedb.org/auth/access?request_token=$requestToken"
                    _loginEvent.emit(LoginEvent.OpenWebView(authUrl))
                    _uiState.value = UiState.Success(Unit)

                    // NOVO: Iniciar polling para verificar se o usuário aprovou
                    startPollingForApproval(requestToken)
                }

                is Result.HttpError -> {
                    val errorMsg = "Erro HTTP ${result.code}: ${result.mensagem}"
                    Log.e("AuthV4ViewModel", errorMsg)
                    _uiState.value = UiState.Error(errorMsg)
                }

                is Result.NetworkError -> {
                    Log.e("AuthV4ViewModel", result.mensagem)
                    _uiState.value = UiState.Error(result.mensagem)
                }

                is Result.UnknownError -> {
                    Log.e("AuthV4ViewModel", result.mensagem)
                    _uiState.value = UiState.Error(result.mensagem)
                }
            }
        }
    }

    // NOVO: Polling para verificar aprovação
    private fun startPollingForApproval(requestToken: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _isPolling.value = true
            Log.d("AuthV4ViewModel", "Iniciando polling para verificar aprovação...")
            var attempts = 0
            val maxAttempts = 20 // 20 tentativas = 1 minuto (3 segundos cada)

            while (attempts < maxAttempts) {
                delay(3000) // Aguardar 3 segundos entre tentativas
                attempts++

                Log.d("AuthV4ViewModel", "Polling tentativa $attempts/$maxAttempts")

                when (val result = repository.createAccessToken(requestToken)) {
                    is Result.Sucesso -> {
                        Log.d("AuthV4ViewModel", "✅ Token aprovado! Access token obtido.")
                        val response = result.data

                        // Salvar no DataStore
                        userPreferences.saveAccessTokenV4(response.access_token)
                        userPreferences.saveAccountId(response.account_id)

                        _isPolling.value = false
                        _loginEvent.emit(LoginEvent.LoginSuccess)
                        _uiState.value = UiState.Success(Unit)
                        return@launch // Sair do loop
                    }

                    is Result.HttpError -> {
                        if (result.code == 401 || result.code == 422) {
                            // Token ainda não foi aprovado - isto é ESPERADO durante polling
                            Log.d("AuthV4ViewModel", "Token ainda não aprovado (${result.code}), tentando novamente em 3s...")
                            // NÃO mudar o estado para Error - manter como Success para não mostrar erro
                        } else {
                            // Outro erro CRÍTICO, parar polling
                            Log.e("AuthV4ViewModel", "ERRO CRÍTICO: ${result.code} - ${result.mensagem}")
                            _isPolling.value = false
                            _uiState.value = UiState.Error("Erro ao verificar aprovação: ${result.mensagem}")
                            return@launch
                        }
                    }

                    is Result.NetworkError -> {
                        Log.d("AuthV4ViewModel", "Erro de rede, tentando novamente em 3s...")
                        // NÃO fazer nada aqui - deixar o loop continuar
                    }

                    is Result.UnknownError -> {
                        Log.d("AuthV4ViewModel", "Erro desconhecido, tentando novamente em 3s...")
                        // NÃO fazer nada aqui - deixar o loop continuar
                    }
                }
            }

            // Se chegou aqui, esgotou as tentativas
            _isPolling.value = false
            Log.e("AuthV4ViewModel", "Tempo limite esgotado. O usuário não aprovou o token.")
            _uiState.value = UiState.Error("Tempo limite esgotado. Por favor, tente novamente.")
        }
    }

    // Passo 3: Converter request token aprovado em access token (via deep link)
    fun completeAuthenticationV4(approvedRequestToken: String) {
        // Cancelar polling se estiver rodando
        pollingJob?.cancel()
        _isPolling.value = false

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            Log.d("AuthV4ViewModel", "Iniciando conversão para access token com: $approvedRequestToken")

            when (val result = repository.createAccessToken(approvedRequestToken)) {
                is Result.Sucesso -> {
                    val response = result.data

                    Log.d("AuthV4ViewModel", "Access Token obtido: ${response.access_token}")
                    Log.d("AuthV4ViewModel", "Account ID: ${response.account_id}")

                    // Salvar no DataStore
                    userPreferences.saveAccessTokenV4(response.access_token)
                    userPreferences.saveAccountId(response.account_id)

                    _loginEvent.emit(LoginEvent.LoginSuccess)
                    _uiState.value = UiState.Success(Unit)
                }

                is Result.HttpError -> {
                    val errorMsg = "Erro ao obter access token: ${result.code} - ${result.mensagem}"
                    Log.e("AuthV4ViewModel", errorMsg)
                    _uiState.value = UiState.Error(errorMsg)
                }

                is Result.NetworkError -> {
                    Log.e("AuthV4ViewModel", result.mensagem)
                    _uiState.value = UiState.Error(result.mensagem)
                }

                is Result.UnknownError -> {
                    Log.e("AuthV4ViewModel", result.mensagem)
                    _uiState.value = UiState.Error(result.mensagem)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        _isPolling.value = false
    }

    class Factory(
        private val repository: RepositoryV4,
        private val userPreferences: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginAuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginAuthViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}