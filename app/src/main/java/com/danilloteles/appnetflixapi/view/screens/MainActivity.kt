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
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.repository.FilmeRepository
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

    val popularMoviesPagingItems = popularFilmeViewModel.popularMoviesStream.collectAsLazyPagingItems()

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
                modifier = Modifier.fillMaxSize().background(BLACK)
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