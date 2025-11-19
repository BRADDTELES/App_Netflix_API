package com.danilloteles.appnetflixapi.view.screens

import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import com.danilloteles.appnetflixapi.viewmodel.LoginViewModel
import com.danilloteles.appnetflixapi.utils.LoginEvent
import com.danilloteles.appnetflixapi.utils.UiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danilloteles.appnetflixapi.constantes.AppDestinations
import com.danilloteles.appnetflixapi.constantes.AppDestinations.LOGIN_SCREEN
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.constantes.AppDestinations.REQUEST_TOKEN_ARG // Import adicionado


@Composable
fun LoginScreen(
    navController: NavController,
    deepLinkRequestToken: String? = null, // Novo parâmetro
    loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.LoginViewModelFactory(
            UserPreferencesRepository(LocalContext.current.applicationContext)
        )
    )
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // LaunchedEffect para lidar com eventos do ViewModel
    LaunchedEffect(Unit) {
        loginViewModel.loginEvent.collect { event ->
            when (event) {
                is LoginEvent.OpenWebView -> {
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.launchUrl(context, Uri.parse(event.url))
                }
                LoginEvent.LoginSuccess -> {
                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    navController.navigate(AppDestinations.MY_LIST_SCREEN) {
                        popUpTo(LOGIN_SCREEN) { inclusive = true } // Garante que a LoginScreen é removida
                    }
                    // Resetar o token no ViewModel para evitar processamento duplicado
                    MainActivity.deeplinkRequestToken.value = null
                }
            }
        }
    }

    // LaunchedEffect para lidar com o deepLinkRequestToken
    LaunchedEffect(deepLinkRequestToken) {
        deepLinkRequestToken?.let { token ->
            loginViewModel.requestToken = token // Define o requestToken no ViewModel
            loginViewModel.createSession() // Inicia a criação da sessão
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BLACK)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.icon_tmdb),
                contentDescription = "Logo do TMDB",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { loginViewModel.authenticateWithTmdb() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VERMELHO,
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .background(
                        color = VERMELHO,
                        shape = RoundedCornerShape(12.dp)
                    ),
            ) {
                Text(
                    text = "Entrar com TMDB",
                    color = WHITE
                )
            }
        }

        when (uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicatorCustom(animationDelay = 400)
                }
            }
            is UiState.Error -> {
                Toast.makeText(context, (uiState as UiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {} // Idle ou Success (não mostra nada na tela de login nesses estados)
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview(){
    // Para o Preview, você precisará mockar o NavController e o ViewModel
    // Não é possível criar uma instância real de NavController ou ViewModel aqui.
    // Pode-se criar um NavController mock ou remover o NavController do Preview se ele não for essencial.
    // LoginScreen(navController = rememberNavController()) // Isso não funcionará no Preview sem um NavHost
}