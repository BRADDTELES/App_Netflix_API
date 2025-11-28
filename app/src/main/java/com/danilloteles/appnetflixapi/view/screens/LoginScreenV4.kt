package com.danilloteles.appnetflixapi.view.screens

import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.constantes.AppDestination
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.repository.v4.FilmeRepositoryV4
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.LoginEvent
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.viewmodel.AuthV4ViewModel

@Composable
fun LoginScreenV4(
    navController: NavController,
    deepLinkRequestToken: String? = null
) {
    val context = LocalContext.current
    val userPreferences = UserPreferencesRepository(context.applicationContext)
    val repository = FilmeRepositoryV4(userPreferences)

    val viewModel: AuthV4ViewModel = viewModel(
        factory = AuthV4ViewModel.Factory(repository, userPreferences)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPolling by viewModel.isPolling.collectAsStateWithLifecycle()

    Log.d("LoginScreenV4", "Composição - deepLinkRequestToken: $deepLinkRequestToken")

    // LaunchedEffect para eventos do ViewModel
    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect { event ->
            Log.d("LoginScreenV4", "Evento recebido: $event")
            when (event) {
                is LoginEvent.OpenWebView -> {
                    Log.d("LoginScreenV4", "Abrindo navegador: ${event.url}")
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, event.url.toUri())
                }

                LoginEvent.LoginSuccess -> {
                    Log.d("LoginScreenV4", "Login v4 realizado com sucesso!")
                    navController.navigate(AppDestination.MINHA_LISTA_SCREEN) {
                        popUpTo(AppDestination.MAIN_SCREEN) { inclusive = false }
                    }
                    // Resetar o token
                    MainActivity.deeplinkRequestToken.value = null
                }
            }
        }
    }

    // LaunchedEffect para processar o deep link (quando o usuário retorna do navegador)
    LaunchedEffect(deepLinkRequestToken) {
        deepLinkRequestToken?.let { token ->
            Log.d("LoginScreenV4", "Deep link detectado com token: $token")
            Log.d("LoginScreenV4", "Iniciando conversão para access token...")
            viewModel.completeAuthenticationV4(token)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BLACK)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.icon_tmdb),
                contentDescription = "Logo do TMDB",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(72.dp))

            Button(
                onClick = {
                    Log.d("LoginScreenV4", "Botão clicado - Iniciando autenticação v4")
                    viewModel.startAuthenticationV4()
                },
                enabled = !isPolling, // Desabilita o botão durante polling
                colors = ButtonDefaults.buttonColors(containerColor = VERMELHO),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .background(color = VERMELHO, shape = RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = if (isPolling) "Aguardando aprovação..." else "Entrar com TMDB (v4)",
                    color = WHITE
                )
            }

            if (isPolling) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aprove no navegador e aguarde...",
                    color = WHITE.copy(alpha = 0.7f)
                )
            }
        }

        when (uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicatorCustom(animationDelay = 1000)
                }
            }

            is UiState.Error -> {
                // Só mostrar erro se NÃO estiver em polling
                // (erros durante polling são esperados)
                if (!isPolling) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context,
                            (uiState as UiState.Error).message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            else -> {} // Idle ou Success
        }
    }
}