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
import com.danilloteles.appnetflixapi.repository.v4.FilmeRepositoryV4Ktor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TesteAPIV4Activity : ComponentActivity() {

    private val userPreferencesRepository by lazy { UserPreferencesRepository(applicationContext) }
    private val repositoryV4 by lazy { FilmeRepositoryV4(userPreferencesRepository) }
    private val repositoryV4Ktor by lazy { FilmeRepositoryV4Ktor() }

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
        // Conteúdo do onCreate será adicionado aqui
        setContent {
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
                                    text = "Após aprovar, o aplicativo será aberto automaticamente.",
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        lifecycleScope.launch {
                                            // Reabrir o navegador, caso o usuário tenha fechado
                                            iniciarFluxoAutenticacao()
                                        }
                                    }
                                ) {
                                    Text("Reabrir Navegador")
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
                // Se a activity for lançada por um deep link, não inicia o fluxo de novo.
                // O onNewIntent ou o processIntent no onCreate/onResume lidará com isso.
                if (intent?.data == null) {
                    iniciarFluxoAutenticacao()
                }
            } else {
                currentUiState = UiState.Success("Já autenticado. Iniciando testes de API.")
                testarChamadasComToken(savedToken)
            }
        }
        
        // Processa o intent inicial caso o app seja aberto por um deep link
        processIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Atualiza o intent da activity
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d("TMDB_V4", "Deep link recebido: $uri")
            if (uri.scheme == "netflixapp" && uri.host == "auth") {
                val requestToken = uri.getQueryParameter("request_token")
                val approved = uri.getQueryParameter("approved")?.toBooleanStrictOrNull() ?: false

                if (!requestToken.isNullOrBlank() && approved) {
                    Log.d("TMDB_V4", "Request token aprovado recebido via deep link: $requestToken")
                    lifecycleScope.launch {
                        trocarRequestPorAccessToken(requestToken)
                    }
                } else if (!approved) {
                    currentUiState = UiState.Error("Usuário não aprovou o acesso ou request token inválido.")
                } else {
                    currentUiState = UiState.Error("Deep link incompleto: falta request_token ou status de aprovação.")
                }
            }
        }
    }

    private suspend fun iniciarFluxoAutenticacao() {
        currentUiState = UiState.Loading
        Log.d("TMDB_V4", "Criando request token com redirect_to...")
        val redirectTo = "netflixapp://callback" // Mantemos o deep link customizado
        when (val req = repositoryV4Ktor.createRequestToken(redirectTo)) {
            is Result.Sucesso -> {
                val requestToken = req.data.request_token
                Log.d("TMDB_V4", "Request token recebido: $requestToken")
                if (!requestToken.isNullOrBlank()) {
                    val url = "https://www.themoviedb.org/auth/access?request_token=$requestToken&redirect_to=$redirectTo"
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

    private suspend fun trocarRequestPorAccessToken(requestToken: String) {
        currentUiState = UiState.Loading
        Log.d("TMDB_V4", "Trocando request token por access token...")
        when (val res = repositoryV4Ktor.createAccessToken(requestToken)) {
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
        when (val criar = repositoryV4Ktor.createList(accessToken, nome = "Minha Lista Kotlin API V4", descricao = "Lista criada via Retrofit")) {
            is Result.Sucesso -> {
                Log.d("TMDB_V4", "CRIAR LISTA -> SUCESSO: ${criar.data}")
                val listId = criar.data.id.toString()

                // Adicionar filme
                Log.d("TMDB_V4", "ADICIONAR FILME -> Adicionando filme com ID 550 à lista $listId")
                when (val adicionar = repositoryV4Ktor.addMovie(accessToken, listId, movieId = 550)) {
                    is Result.Sucesso -> Log.d("TMDB_V4", "ADICIONAR FILME -> SUCESSO: ${adicionar.data}")
                    is Result.HttpError -> currentUiState = UiState.Error("ADICIONAR FILME -> ERRO HTTP: ${adicionar.mensagem}")
                    is Result.NetworkError -> currentUiState = UiState.Error("ADICIONAR FILME -> ERRO DE REDE")
                    is Result.UnknownError -> currentUiState = UiState.Error("ADICIONAR FILME -> ERRO DESCONHECIDO: ${adicionar.mensagem}")
                }

                // Detalhes da lista
                Log.d("TMDB_V4", "DETALHES DA LISTA -> Buscando detalhes da lista $listId")
                when (val detalhes = repositoryV4Ktor.getListDetails(accessToken, listId)) {
                    is Result.Sucesso -> Log.d("TMDB_V4", "DETALHES DA LISTA -> SUCESSO: ${detalhes.data}")
                    is Result.HttpError -> currentUiState = UiState.Error("DETALHES DA LISTA -> ERRO HTTP: ${detalhes.mensagem}")
                    is Result.NetworkError -> currentUiState = UiState.Error("DETALHES DA LISTA -> ERRO DE REDE")
                    is Result.UnknownError -> currentUiState = UiState.Error("DETALHES DA LISTA -> ERRO DESCONHECIDO: ${detalhes.mensagem}")
                }
            }
            is Result.HttpError -> currentUiState = UiState.Error("CRIAR LISTA -> ERRO HTTP: ${criar.mensagem}")
            is Result.NetworkError -> currentUiState = UiState.Error("CRIAR LISTA -> ERRO DE REDE")
            is Result.UnknownError -> currentUiState = UiState.Error("CRIAR LISTA -> ERRO DESCONHECIDO: ${criar.mensagem}")
        }
    }

}
