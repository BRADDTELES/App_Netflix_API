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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.danilloteles.appnetflixapi.enums.FabState
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.repository.v3.FilmeRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.MenuSection
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.view.componentes.PopularMoviesSection
import com.danilloteles.appnetflixapi.view.navigation.NetflixApp
import com.danilloteles.appnetflixapi.viewmodel.PopularFilmeViewModel
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

        // Processar intent inicial (quando o app é aberto pelo deep link)
        handleIntent(intent)

        setContent {
            NetflixApp(deeplinkRequestToken = deeplinkRequestToken)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // IMPORTANTE: Atualizar o intent atual
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        Log.d("MainActivity", "=== handleIntent chamado ===")
        Log.d("MainActivity", "Intent action: ${intent?.action}")
        Log.d("MainActivity", "Intent data: ${intent?.data}")

        intent?.data?.let { uri ->
            Log.d("MainActivity", "URI completo: $uri")
            Log.d("MainActivity", "Scheme: ${uri.scheme}")
            Log.d("MainActivity", "Host: ${uri.host}")
            Log.d("MainActivity", "Path: ${uri.path}")
            Log.d("MainActivity", "Query: ${uri.query}")

            // Verificar se é o deep link esperado
            if (uri.scheme == "netflixapp" && uri.host == "auth") {

                // Tentar extrair o request_token de diferentes formas
                val requestToken = uri.getQueryParameter("request_token")

                if (requestToken != null && requestToken != "TEST_TOKEN") {
                    Log.d("MainActivity", "✅ Request token extraído com sucesso: $requestToken")
                    deeplinkRequestToken.value = requestToken
                } else if (requestToken == null) {
                    Log.w(
                        "MainActivity",
                        "⚠️ Deep link sem parâmetros - ignorando (provavelmente do TMDB)"
                    )
                    Log.w("MainActivity", "O polling vai detectar a aprovação automaticamente")
                    // NÃO fazer nada aqui - deixar o polling funcionar
                } else {
                    Log.d("MainActivity", "🧪 Token de teste detectado: $requestToken")
                    deeplinkRequestToken.value = requestToken
                }

                // Tentar extrair manualmente da query string
                uri.query?.let { query ->
                    Log.d("MainActivity", "Query string completa: $query")
                    val tokenMatch = Regex("request_token=([^&]+)").find(query)
                    if (tokenMatch != null) {
                        val extractedToken = tokenMatch.groupValues[1]
                        Log.d("MainActivity", "✅ Token extraído manualmente: $extractedToken")
                        deeplinkRequestToken.value = extractedToken
                    }
                }
            }else {
                Log.w("MainActivity", "Deep link não corresponde ao esperado")
                Log.w("MainActivity", "Esperado: scheme=netflixapp, host=auth")
                Log.w("MainActivity", "Recebido: scheme=${uri.scheme}, host=${uri.host}")
            }
        } ?: Log.d("MainActivity", "Intent.data é nulo - não é um deep link")
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetflixScreen(
    onMovieClick: (MediaItem) -> Unit,
    onMyListClick: () -> Unit,
    onSeriesListClick: () -> Unit,
    onFilmesListClick: () -> Unit
) {
    val filmeApi = RetrofitHelper.filmeAPI
    val filmeRepository = remember { FilmeRepository(filmeApi) }

    val popularFilmeViewModel: PopularFilmeViewModel = viewModel(
        factory = PopularFilmeViewModel.PopularMoviesViewModelFactory(filmeRepository)
    )

    val popularMoviesPagingItems =
        popularFilmeViewModel.popularMoviesStream.collectAsLazyPagingItems()

    val listState = rememberLazyGridState()
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
            MenuSection(
                onMyListClick = onMyListClick,
                onSeriesListClick = onSeriesListClick,
                onFilmesListClick = onFilmesListClick
            )

            PopularMoviesSection(
                filmesPaginados = popularMoviesPagingItems,
                onMovieClick = onMovieClick,
                lazyGridState = listState
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BLACK)
            ) {
                if (popularMoviesPagingItems.loadState.refresh is LoadState.Loading) {
                    LoadingIndicatorCustom(
                        animationDelay = 1000,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }

                if (popularMoviesPagingItems.loadState.refresh is LoadState.Error) {
                    Text(
                        text = "Falha ao carregar. Verifique sua conexão.",
                        color = WHITE,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
        onMyListClick = {},
        onSeriesListClick = {},
        onFilmesListClick = {}
    )
}