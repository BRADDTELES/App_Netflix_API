@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.view.componentes.OutlinedTextFieldCustom
import com.danilloteles.appnetflixapi.viewmodel.MovieDetailsViewModel

@Composable
fun MovieForm(
    movieId: Int
) {

    if (movieId == 0) {
        MovieFormContent(filme = null)
    } else {

        val viewModel: MovieDetailsViewModel = viewModel(
            factory = MovieDetailsViewModel.Factory(movieId)
        )
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        when (val state = uiState) {
            is UiState.Idle -> {}
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(BLACK),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicatorCustom(animationDelay = 400)
                }
            }
            is UiState.Success -> {
                MovieFormContent(filme = state.data)
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(BLACK),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = WHITE)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MovieFormContent(
    filme: FilmeDetalhes?
) {
    var título by remember { mutableStateOf(filme?.title ?: "") }

    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BLACK)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            val aspectRatioValue = if (filme != null) (1.5f / 2f) else (4f / 3f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatioValue)
            ) {
                AsyncImage(
                    model = if (filme != null) Constantes.IMAGE_BASE_URL + filme.poster_path else R.drawable.capa,
                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                    error = painterResource(id = R.drawable.capa),
                    contentDescription = "Imagem da capa do filme",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                SmallFloatingActionButton(
                    onClick = { /* TODO: Ação para abrir a Galeria do celular */ },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    contentColor = WHITE,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "Botão de editar"
                    )
                }
            }

            OutlinedTextFieldCustom(
                value = título,
                onValueChange = { título = it },
                label = "Título",
                modifier = Modifier.fillMaxWidth().padding(20.dp, 32.dp),
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
            Button(
                onClick = {
                    /* TODO: Ação do botão salvar ou editar o filme */
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VERMELHO
                ),
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(
                        color = VERMELHO,
                        shape = RoundedCornerShape(6.dp)
                    )
            ) {

                Icon(
                    imageVector = Icons.Default.SaveAs,
                    contentDescription = "Salvar",
                    tint = WHITE
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SALVAR",
                    color = WHITE,
                    fontWeight = FontWeight.SemiBold
                )

            }
        }
    }
}

@Preview
@Composable
private fun MovieFormPreview_NewMovie(){
    MovieFormContent(filme = null)
}

@Preview
@Composable
fun MovieFormPreview_EditMovie() {
    MovieFormContent(
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
        )
    )
}