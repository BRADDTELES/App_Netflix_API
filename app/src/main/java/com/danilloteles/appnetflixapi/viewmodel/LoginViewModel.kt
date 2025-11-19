package com.danilloteles.appnetflixapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.danilloteles.appnetflixapi.api.FilmeAPI
import com.danilloteles.appnetflixapi.model.CreateSessionRequest
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.LoginEvent
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
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
                val response = filmeAPI.createRequestToken()
                if (response.isSuccessful) {
                    response.body()?.let { tokenResponse ->
                        if (tokenResponse.success) {
                            requestToken = tokenResponse.request_token
                            val authUrl = "https://www.themoviedb.org/authenticate/$requestToken"
                            _loginEvent.emit(LoginEvent.OpenWebView(authUrl))
                            _uiState.value = UiState.Success(Unit)
                        } else {
                            _uiState.value = UiState.Error("Falha ap obter request token.")
                            Log.e(
                                "LoginViewModel",
                                "Falha ap obter request token: ${tokenResponse.request_token}"
                            )
                        }
                    } ?: run {
                        _uiState.value = UiState.Error("Resposta vazia ao obter request token.")
                    }
                } else {
                    _uiState.value = UiState.Error("Erro HTTP ao obter request token.")
                    Log.e("LoginViewModel", "Erro HTTP ao obter request token: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão ao obter request token.")
                Log.e("LoginViewModel", "Erro: ${e.message}", e)
            }
        }
    }

    fun createSession() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            requestToken?.let { token ->
                try {
                    val response =
                        filmeAPI.createSessionId(CreateSessionRequest(request_token = token))
                    if (response.isSuccessful) {
                        response.body()?.let { sessionIdResponse ->
                            if (sessionIdResponse.success) {
                                userPreferencesRepository.saveSessionId(sessionIdResponse.session_id)
                                _loginEvent.emit(LoginEvent.LoginSuccess)
                                _uiState.value = UiState.Success(Unit)
                            } else {
                                _uiState.value = UiState.Error("Falha ao criar session id.")
                                Log.e(
                                    "LoginViewModel",
                                    "Falha ao criar sessão: ${sessionIdResponse.success}"
                                )
                            }
                        } ?: run {
                            _uiState.value = UiState.Error("Resposta vazia ao criar session id.")
                            Log.e("LoginViewModel", "Resposta vazia ao criar session id.")
                        }
                    } else {
                        _uiState.value = UiState.Error("Erro HTTP ao criar session id.")
                        Log.e("LoginViewModel", "Erro HTTP ao criar session id: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Erro de conexão ao criar session id")
                    Log.e("LoginViewModel", "Erro: ${e.message}", e)
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