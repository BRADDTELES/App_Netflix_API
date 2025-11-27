package com.danilloteles.appnetflixapi.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.danilloteles.appnetflixapi.common.Result
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.repository.v4.FilmeRepositoryV4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TesteAPIV4Activity : ComponentActivity() {

    private val userPreferencesRepository by lazy { UserPreferencesRepository(applicationContext) }
    private val repositoryV4 by lazy { FilmeRepositoryV4(userPreferencesRepository) }

    // Estados da UI para o novo fluxo
    private sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object AwaitingUserInput : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private var currentUiState by mutableStateOf<UiState>(UiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var pastedToken by remember { mutableStateOf("") }

            // Usando o MaterialTheme corretamente (Material 3)
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val state = currentUiState) {
                        is UiState.Idle, is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.AwaitingUserInput -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "O navegador foi aberto para você aprovar o acesso.",
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Após aprovar, copie o 'Token de Requisição' da página do TMDB e cole no campo abaixo.",
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = pastedToken,
                                    onValueChange = { pastedToken = it },
                                    label = { Text("Cole o token aqui") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (pastedToken.isNotBlank()) {
                                            lifecycleScope.launch {
                                                verificarTokenColado(pastedToken)
                                            }
                                        }
                                    },
                                    enabled = pastedToken.isNotBlank()
                                ) {
                                    Text("Verificar Token")
                                }
                            }
                        }
                        is UiState.Success -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(state.message)
                                    Text("Verifique o Logcat para detalhes dos testes.", modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                        is UiState.Error -> {
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Erro: ${state.message}", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Lança a autenticação se ainda não tiver um token
        lifecycleScope.launch {
            val savedToken = userPreferencesRepository.accessTokenV4.first()
            if (savedToken.isNullOrBlank()) {
                iniciarFluxoAutenticacao()
            } else {
                currentUiState = UiState.Success("Já autenticado. Iniciando testes de API.")
                testarChamadasComToken(savedToken)
            }
        }
    }

    private suspend fun iniciarFluxoAutenticacao() {
        currentUiState = UiState.Loading
        Log.d("TMDB_V4", "Criando request token (sem redirect_to)...")
        when (val req = repositoryV4.createRequestToken(null)) {
            is Result.Sucesso -> {
                val requestToken = req.data.request_token
                Log.d("TMDB_V4", "Request token recebido: $requestToken")
                if (!requestToken.isNullOrBlank()) {
                    val url = "https://www.themoviedb.org/auth/access?request_token=$requestToken"
                    Log.d("TMDB_V4", "Abrindo navegador externo com URL: $url")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    currentUiState = UiState.AwaitingUserInput
                } else {
                    currentUiState = UiState.Error("Request token vazio.")
                }
            }
            is Result.HttpError -> currentUiState = UiState.Error("Erro HTTP ao obter request token: ${req.mensagem}")
            is Result.NetworkError -> currentUiState = UiState.Error("Erro de rede ao obter request token.")
            is Result.UnknownError -> currentUiState = UiState.Error("Erro inesperado ao obter request token: ${req.mensagem}")
        }
    }

    private suspend fun verificarTokenColado(requestToken: String) {
        Log.d("TMDB_V4", "Verificando token colado pelo usuário: $requestToken")
        trocarRequestPorAccessToken(requestToken)
    }

    private suspend fun trocarRequestPorAccessToken(requestToken: String) {
        currentUiState = UiState.Loading
        Log.d("TMDB_V4", "Trocando request token por access token...")
        when (val res = repositoryV4.createAccessToken(requestToken)) {
            is Result.Sucesso -> {
                val accessToken = res.data.access_token
                val accountId = res.data.account_id
                Log.d("TMDB_V4", "Access token recebido: $accessToken, Account ID: $accountId")
                if (!accessToken.isNullOrBlank() && !accountId.isNullOrBlank()) {
                    userPreferencesRepository.saveAccessTokenV4(accessToken)
                    userPreferencesRepository.saveAccountId(accountId)
                    Log.d("TMDB_V4", "Access token e Account ID salvos no DataStore")
                    testarChamadasComToken(accessToken)
                    currentUiState = UiState.Success("Autenticação bem-sucedida! Tokens salvos.")
                } else {
                    currentUiState = UiState.Error("Access token ou Account ID vazio na resposta.")
                }
            }
            is Result.HttpError -> currentUiState = UiState.Error("Erro HTTP ao obter access token: ${res.mensagem}")
            is Result.NetworkError -> currentUiState = UiState.Error("Erro de rede ao obter access token.")
            is Result.UnknownError -> currentUiState = UiState.Error("Erro inesperado ao obter access token: ${res.mensagem}")
        }
    }

    private suspend fun testarChamadasComToken(accessToken: String) {
        Log.d("TMDB_V4", "TESTE INICIADO... usando token")
        // Criar Lista
        when (val criar = repositoryV4.createList(accessToken, nome = "Minha Lista Kotlin API V4", descricao = "Lista criada via Retrofit")) {
            is Result.Sucesso -> {
                Log.d("TMDB_V4", "CRIAR LISTA -> SUCESSO: ${criar.data}")
                val listId = criar.data.id.toString()

                // Adicionar filme
                Log.d("TMDB_V4", "ADICIONAR FILME -> Adicionando filme com ID 550 à lista $listId")
                when (val adicionar = repositoryV4.addMovie(accessToken, listId, movieId = 550)) {
                    is Result.Sucesso -> Log.d("TMDB_V4", "ADICIONAR FILME -> SUCESSO: ${adicionar.data}")
                    is Result.HttpError -> Log.d("TMDB_V4", "ADICIONAR FILME -> ERRO HTTP: ${adicionar.mensagem}")
                    is Result.NetworkError -> Log.d("TMDB_V4", "ADICIONAR FILME -> ERRO DE REDE")
                    is Result.UnknownError -> Log.d("TMDB_V4", "ADICIONAR FILME -> ERRO DESCONHECIDO: ${adicionar.mensagem}")
                }

                // Detalhes da lista
                Log.d("TMDB_V4", "DETALHES DA LISTA -> Buscando detalhes da lista $listId")
                when (val detalhes = repositoryV4.getListDetails(accessToken, listId)) {
                    is Result.Sucesso -> Log.d("TMDB_V4", "DETALHES DA LISTA -> SUCESSO: ${detalhes.data}")
                    is Result.HttpError -> Log.d("TMDB_V4", "DETALHES DA LISTA -> ERRO HTTP: ${detalhes.mensagem}")
                    is Result.NetworkError -> Log.d("TMDB_V4", "DETALHES DA LISTA -> ERRO DE REDE")
                    is Result.UnknownError -> Log.d("TMDB_V4", "DETALHES DA LISTA -> ERRO DESCONHECIDO: ${detalhes.mensagem}")
                }
            }
            is Result.HttpError -> Log.d("TMDB_V4", "CRIAR LISTA -> ERRO HTTP: ${criar.mensagem}")
            is Result.NetworkError -> Log.d("TMDB_V4", "CRIAR LISTA -> ERRO DE REDE")
            is Result.UnknownError -> Log.d("TMDB_V4", "CRIAR LISTA -> ERRO DESCONHECIDO: ${criar.mensagem}")
        }
    }
}