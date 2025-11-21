package com.danilloteles.appnetflixapi.view.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilloteles.appnetflixapi.enums.FabState
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.MenuSection
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.view.componentes.PopularMoviesSection
import com.danilloteles.appnetflixapi.view.navigation.NetflixApp
import com.danilloteles.appnetflixapi.viewmodel.PopularMoviesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class MainActivity : ComponentActivity() {

    companion object {
        val deeplinkRequestToken: MutableStateFlow<String?> = MutableStateFlow(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NetflixApp(deeplinkRequestToken = deeplinkRequestToken)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("TAG-MainActivity", "onNewIntent chamado com URI: ${intent.data}")
        intent.data?.let { uri ->
            if (  uri.scheme == "netflixapp" && uri.host == "auth"  ) {
                val requestToken = uri.getQueryParameter("request_token")
                Log.d("TAG-MainActivity", "Deep link reconhecido. Request token extraído: $requestToken")
                deeplinkRequestToken.value = requestToken
                Log.d("TAG-MainActivity", "deeplinkRequestToken.value atualizado para: ${deeplinkRequestToken.value}")
            } else {
                Log.d("TAG-MainActivity", "URI do deep link não corresponde aos critérios.")
                Log.d("TAG-MainActivity", "Esperado scheme: netflixapp, host: auth")
                Log.d("TAG-MainActivity", "URI recebida: $uri")
                Log.d("TAG-MainActivity", "Scheme recebido: ${uri.scheme}, Host recebido: ${uri.host}")
            }
        } ?: run {
            Log.d("TAG-MainActivity", "Intent.data é nulo em onNewIntent.")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetflixScreen(
    onMovieClick: (Filme) -> Unit,
    onAddClick: () -> Unit,
    onMyListClick: () -> Unit // Novo parâmetro
) {
    // Instanciando o ViewModel
    val popularMoviesViewModel: PopularMoviesViewModel = viewModel()
    // Coletando o estado da UI
    val uiState by popularMoviesViewModel.uiState.collectAsStateWithLifecycle()

    val listState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    var fabState by remember { mutableStateOf(FabState.EXPANDED) }

    // Lógica para observar a rolagem e determinar o estado do FAB
    LaunchedEffect(listState) {
        var lastKnownScrollOffset = 0
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { currentScrollOffset ->
                fabState = if (listState.firstVisibleItemIndex == 0) {
                    FabState.EXPANDED
                } else if (currentScrollOffset > lastKnownScrollOffset) {
                    FabState.HIDDEN
                } else {
                    FabState.COLLAPSED
                }
                lastKnownScrollOffset = currentScrollOffset
            }
    }

    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            MenuSection(onMyListClick = onMyListClick) // Passando o onMyListClick

            when (val state = uiState) {
                is UiState.Idle -> {}
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(BLACK),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicatorCustom(animationDelay = 1000)
                    }
                }
                is UiState.Success -> {
                    PopularMoviesSection(
                        listFilme = state.data,
                        onMovieClick = onMovieClick,
                        lazyGridState = listState
                    )
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = WHITE
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun NetflixScreenPreview() {
    NetflixScreen(
        onMovieClick = {},
        onAddClick = {},
        onMyListClick = {} // Placeholder para o novo parâmetro
    )
}