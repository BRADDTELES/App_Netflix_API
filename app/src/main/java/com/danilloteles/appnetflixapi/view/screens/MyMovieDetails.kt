
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AddToQueue
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
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
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.model.FilmeDetalhes
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.GRAY_100
import com.danilloteles.appnetflixapi.ui.theme.GRAY_900
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.viewmodel.MovieDetailsViewModel

@Composable
fun MyMovieDetails(
    movieId: Int,
    onClick: (Int) -> Unit
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
            ConteudoMyMovieDetails(
                filme = state.data,
                onClick = onClick
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
fun ConteudoMyMovieDetails(
    filme: FilmeDetalhes,
    onClick: (Int) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    BottomSheetScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        sheetContent = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model =
                        /*Constantes.IMAGE_BASE_URL + filme.poster_path,*/
                        R.drawable.movie_show_vizinha,
                    contentDescription = "Capa do filme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                    contentScale = ContentScale.Crop
                )

            }
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = 400.dp,
        sheetMaxWidth = 620.dp,
        sheetContainerColor = GRAY_900,
        sheetDragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .background(GRAY_100, MaterialTheme.shapes.extraLarge)
                )
                Spacer(Modifier.height(16.dp))
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Netflix",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = BLACK,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(BLACK)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        var splitButtonChecked by remember { mutableStateOf(false) }

                        SplitButtonLayout(
                            leadingButton = {
                                SplitButtonDefaults.TonalLeadingButton(
                                    onClick = { /* Ação principal, se houver */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WHITE,
                                        contentColor = GRAY_900
                                    )
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
                                val description = "Toggle Button"
                                // Icon-only trailing button should have a tooltip for a11y.
                                TooltipBox(
                                    positionProvider =
                                        TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                    tooltip = { PlainTooltip { Text(description) } },
                                    state = rememberTooltipState(),
                                ) {
                                    SplitButtonDefaults.TonalTrailingButton(
                                        checked = splitButtonChecked,
                                        onCheckedChange = { splitButtonChecked = it },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = WHITE,
                                            contentColor = GRAY_900
                                        ),
                                        modifier =
                                            Modifier.semantics {
                                                stateDescription = if (splitButtonChecked) "Expanded" else "Collapsed"
                                                contentDescription = description
                                            },
                                    ) {
                                        val rotation: Float by
                                        animateFloatAsState(
                                            targetValue = if (splitButtonChecked) 180f else 0f,
                                            label = "Rotacionar ícone",
                                        )
                                        Icon(
                                            Icons.Filled.KeyboardArrowDown,
                                            modifier =
                                                Modifier.size(SplitButtonDefaults.TrailingIconSize).graphicsLayer {
                                                    this.rotationZ = rotation
                                                },
                                            contentDescription = "Expandir menu",
                                        )
                                    }
                                }
                            },
                        )
                        DropdownMenu(
                            expanded = splitButtonChecked,
                            onDismissRequest = { splitButtonChecked = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Adicionar") },
                                onClick = {
                                    /* TODO: Ação de Adicionar o filme na lista aqui */
                                    onClick(filme.id)
                                    splitButtonChecked = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.AddToQueue, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text("Deletar") },
                                onClick = {
                                    /* TODO: Ação de Deletar o filme da lista aqui */
                                    onClick(filme.id)
                                    splitButtonChecked = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                Text(
                    text =
                        /*filme.title,*/
                        "Movie title",
                    color = WHITE,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Text(
                    text =
                        /*filme.overview,*/
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis vestibulum semper eros ut faucibus. Aenean ultricies volutpat dapibus. Aenean quis malesuada est, sit amet porttitor neque. Nunc faucibus lacus neque, ac sodales nunc dignissim a. Ut libero ante, tincidunt vitae luctus sed, feugiat in felis. Sed volutpat consectetur nulla ut ullamcorper. Integer nibh magna, scelerisque vel orci nec, convallis eleifend metus. Pellentesque pulvinar mauris id leo luctus, sit amet blandit felis luctus. Curabitur nec vulputate lectus. Fusce consectetur felis vel pretium viverra. Fusce mattis elit at nisl luctus, vitae ultricies erat pharetra. Suspendisse eu accumsan neque. Sed elementum nibh eu maximus mollis. Proin cursus ex vel est luctus ultricies. Nulla mollis rhoncus fermentum.\n" +
                                "\n" +
                                "Ut sapien felis, placerat ut eleifend id, vestibulum non leo. Phasellus ac sapien ut odio faucibus aliquet sit amet ac ligula. Curabitur ultrices eleifend nibh id iaculis. Etiam dictum arcu eu quam dictum ultrices. Quisque ut eros nisi. Nulla vitae posuere ex. Praesent venenatis nulla eget mattis pellentesque. Fusce nec dictum nisl.\n" +
                                "\n" +
                                "Maecenas diam mauris, maximus non risus et, pulvinar faucibus nibh. Nunc ultricies sodales convallis. Phasellus rhoncus eu neque id finibus. Sed eu vehicula tortor. Nullam convallis erat ut quam bibendum venenatis. Vivamus suscipit nisi at est mollis interdum. Sed placerat nulla in tortor convallis, eu finibus dolor tincidunt. Vestibulum efficitur, mauris eget euismod rhoncus, neque dui maximus orci, non rutrum nisl eros ac ex. Morbi aliquam porttitor velit in pharetra. Mauris id feugiat felis, ac fringilla nibh. Aenean pulvinar, velit eu eleifend semper, metus nisi facilisis enim, ut hendrerit ipsum mauris eget quam.",
                    color = WHITE,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ConteudoMyMovieDetailsPreview() {
    ConteudoMyMovieDetails(
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
        onClick = {}
    )
}
