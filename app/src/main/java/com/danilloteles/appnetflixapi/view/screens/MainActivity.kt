package com.danilloteles.appnetflixapi.view.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.view.navigation.NetflixApp
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.view.componentes.FloatingActionButtonCustom
import com.danilloteles.appnetflixapi.view.componentes.MenuSection
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.view.componentes.PopularMoviesSection
import com.danilloteles.appnetflixapi.viewmodel.PopularMoviesViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NetflixApp()
        }
    }
}

@Composable
fun NetflixScreen(
    onMovieClick: (Filme) -> Unit,
    onAddClick: () -> Unit
) {

    // Instanciando o ViewModel
    val popularMoviesViewModel: PopularMoviesViewModel = viewModel()

    // Coletando o estado da UI
    val uiState by popularMoviesViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            NetflixTopBar()
        },
        floatingActionButton = {
            FloatingActionButtonCustom(
                onAddClick = onAddClick
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

            MenuSection()

            when ( val state = uiState ){
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VERMELHO)
                    }
                }
                is UiState.Success -> {
                    PopularMoviesSection(
                        listFilme = state.movies,
                        onMovieClick = onMovieClick
                    )
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
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
        onAddClick = {}
    )
}