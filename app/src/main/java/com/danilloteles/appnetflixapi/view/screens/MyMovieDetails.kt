
package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.viewmodel.MovieDetailsViewModel

@Composable
fun MyMovieDetailsScreen(
    movieId: Int,
    onEditClick: (Int) -> Unit
) {
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
            MyMovieDetailContent(
                filme = state.data,
                onEditClick = onEditClick
            )
        }

        is UiState.Error -> {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyMovieDetailContent(
    filme: FilmeDetalhes,
    onEditClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var splitButtonChecked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
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
                    contentDescription = "Capa do filme",
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
                    Box {
                        SplitButtonLayout(
                            leadingButton = {
                                SplitButtonDefaults.LeadingButton(
                                    onClick = { /* Ação principal, se houver */ },
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
                                        contentDescription = "Ações do Filme",
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text("Opções")
                                }
                            },
                            trailingButton = {
                                SplitButtonDefaults.TrailingButton(
                                    checked = splitButtonChecked,
                                    onCheckedChange = { splitButtonChecked = it },
                                    modifier =
                                    Modifier.semantics {
                                        stateDescription = if (splitButtonChecked) "Expanded" else "Collapsed"
                                    },
                                ) {
                                    val rotation: Float by
                                    animateFloatAsState(
                                        targetValue = if (splitButtonChecked) 180f else 0f,
                                        label = "Trailing Icon Rotation",
                                    )
                                    Icon(
                                        Icons.Filled.KeyboardArrowDown,
                                        modifier =
                                        Modifier
                                            .size(SplitButtonDefaults.TrailingIconSize)
                                            .graphicsLayer {
                                                this.rotationZ = rotation
                                            },
                                        contentDescription = "Expandir menu",
                                    )
                                }
                            },
                        )

                        DropdownMenu(
                            expanded = splitButtonChecked,
                            onDismissRequest = { splitButtonChecked = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    onEditClick(filme.id)
                                    splitButtonChecked = false
                                 },
                                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { /* Handle settings! */ },
                                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Send Feedback") },
                                onClick = { /* Handle send feedback! */ },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                                trailingIcon = { Text("F11", textAlign = TextAlign.Center) },
                            )
                        }
                    }
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

@Preview
@Composable
private fun MyMovieDetailsScreenPreview() {
    MyMovieDetailContent(
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
        onEditClick = {}
    )
}
