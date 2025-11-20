@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.viewmodel.MovieDetailsViewModel

@Composable
fun MovieDetails(
    movieId: Int
) {
    val viewModel: MovieDetailsViewModel = viewModel(
        factory = MovieDetailsViewModel.Factory(movieId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    when (val state = uiState) {
        is UiState.Idle -> {}
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BLACK),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicatorCustom(animationDelay = 400)
            }
        }

        is UiState.Success -> {
            ConteudoFilme(
                filme = state.data,
                snackbarHostState = snackbarHostState
            )
        }

        is UiState.Error -> {
            // O erro agora é tratado pelo Snackbar.
            // Exibir um estado vazio ou um componente que permita ao usuário tentar novamente.
            // Por simplicidade, exibimos uma caixa vazia.
            Box(modifier = Modifier.fillMaxSize().background(BLACK))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConteudoFilme(
    filme: FilmeDetalhes,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            NetflixTopBar()
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BLACK)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = Constantes.IMAGE_BASE_URL + filme.poster_path,
                    contentDescription = "Imagem da capa do filme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f / 2f),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = filme.title,
                color = WHITE,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = filme.overview,
                color = WHITE,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ConteudoFilmePreview() {
    ConteudoFilme(
        filme = FilmeDetalhes(
            adult = false,
            backdrop_path = "",
            belongs_to_collection = "",
            budget = 0,
            genres = emptyList(),
            homepage = "",
            id = 0,
            imdb_id = "",
            original_language = "",
            original_title = "",
            overview = "This is a test movie for preview.",
            popularity = 0.0,
            poster_path = "/t6HIqrRAFyUMC6bZqMfPSzPNw0s.jpg",
            production_companies = emptyList(),
            production_countries = emptyList(),
            release_date = "",
            revenue = 0,
            runtime = 0,
            spoken_languages = emptyList(),
            status = "",
            tagline = "",
            title = "Movie title",
            video = false,
            vote_average = 0.0,
            vote_count = 0
        ),
        snackbarHostState = SnackbarHostState()
    )
}