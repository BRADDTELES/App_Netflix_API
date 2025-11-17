package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ModeEditOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.DetailsUiState
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.viewmodel.MovieDetailsViewModel

@Composable
fun MovieDetails(
    movieId: Int,
    onEditClick: (Int) -> Unit
) {
    val viewModel: MovieDetailsViewModel = viewModel(
        factory = MovieDetailsViewModel.Factory(movieId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DetailsUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BLACK),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VERMELHO)
            }
        }

        is DetailsUiState.Success -> {
            ConteudoFilme(
                filme = state.movie,
                onEditClick = onEditClick
            )
        }

        is DetailsUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BLACK),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.message, color = WHITE)
            }
        }
    }


}

@Composable
fun ConteudoFilme(
    filme: Filme,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            NetflixTopBar()
        }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    BotaoEditar(
                        onClick = { onEditClick(filme.id) }
                    )
                }
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

@Composable
fun BotaoEditar(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                color = TRANSPARENT
            )
            .size(60.dp)
            .border(
                width = 2.dp,
                color = WHITE,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Botão de editar",
            tint = WHITE
        )
    }
}

@Preview
@Composable
private fun ConteudoFilmePreview() {
    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->
        ConteudoFilme(
            filme = Filme(
                id = 1,
                title = "Filme de Teste",
                poster_path = "/t6HIqrRAFyUMC6bZqMfPSzPNw0s.jpg",
                adult = false,
                backdrop_path = "",
                original_language = "en-US",
                original_title = "Test Movie",
                overview = "This is a test movie for preview.",
                popularity = 100.0,
                release_date = "2025-11-16",
                video = false,
                vote_average = 7.0,
                vote_count = 100,
                genre_ids = emptyList(),
            ),
            onEditClick = {},
            modifier = Modifier.padding(paddingValues)
        )
    }
}