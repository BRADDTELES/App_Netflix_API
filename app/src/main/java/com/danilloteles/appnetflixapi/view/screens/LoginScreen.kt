package com.danilloteles.appnetflixapi.view.screens

import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.danilloteles.appnetflixapi.repository.v4.RepositoryV4
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.LoginEvent
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.viewmodel.LoginAuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    deepLinkRequestToken: String? = null,
    destinationRoute: String = AppDestination.MINHA_LISTA_SCREEN // Destino padrão
) {
    val context = LocalContext.current
    val userPreferences = UserPreferencesRepository(context.applicationContext)
    // A AuthV4ViewModel precisa do seu próprio repositório para as chamadas de autenticação
    val authRepository = RepositoryV4(userPreferences)

    val viewModel: LoginAuthViewModel = viewModel(
        factory = LoginAuthViewModel.Factory(authRepository, userPreferences)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPolling by viewModel.isPolling.collectAsStateWithLifecycle()

    Log.d("LoginScreenV4", "Composição - deepLinkRequestToken: $deepLinkRequestToken, Destino: $destinationRoute")

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
                    Log.d("LoginScreenV4", "Login v4 realizado com sucesso! Navegando para: $destinationRoute")
                    navController.navigate(destinationRoute) {
                        // Remove a tela de login da pilha de navegação de forma correta
                        popUpTo(AppDestination.LOGIN_ROUTE_PATTERN) {
                            inclusive = true
                        }
                        // Garante que a tela de destino seja a única no topo
                        launchSingleTop = true
                    }
                    // Resetar o token, se houver
                    MainActivity.deeplinkRequestToken.value = null
                }
            }
        }
    }

    // LaunchedEffect para processar o deep link (se o usuário retornar do navegador)
    LaunchedEffect(deepLinkRequestToken) {
        deepLinkRequestToken?.let { token ->
            Log.d("LoginScreenV4", "Deep link detectado com token: $token. O polling cuidará disso.")
            // Apenas limpamos o valor para não ser processado novamente, pois o polling é o método principal
            MainActivity.deeplinkRequestToken.value = null
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

        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicatorCustom(animationDelay = 1000)
                }
            }

            is UiState.Error -> {
                if (!isPolling) {
                    LaunchedEffect(state.message) {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            else -> {} // Idle ou Success
        }
    }
}