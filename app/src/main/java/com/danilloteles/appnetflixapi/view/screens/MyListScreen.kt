@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilloteles.appnetflixapi.model.Filme
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.UiState
import com.danilloteles.appnetflixapi.utils.material3expressive.ConnectedButtonGroupComposable
import com.danilloteles.appnetflixapi.utils.material3expressive.ConnectedButtonGroupComposableCustom
import com.danilloteles.appnetflixapi.utils.material3expressive.FabMenuColorScheme
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.PopularMoviesSection
import com.danilloteles.appnetflixapi.viewmodel.MyListViewModel // Import alterado
import com.danilloteles.appnetflixapi.utils.UserPreferencesRepository // Import adicionado para o ViewModelFactory
import androidx.compose.ui.platform.LocalContext // Import adicionado para o ViewModelFactory
import androidx.compose.runtime.LaunchedEffect // Import adicionado

@Composable
fun MyListScreen(
    onMovieClick: (Filme) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current // Adicionado para o ViewModelFactory
    val myListViewModel: MyListViewModel = viewModel( // Instância alterada
        factory = MyListViewModel.MyListViewModelFactory(
            UserPreferencesRepository(context)
        )
    )
    val uiState by myListViewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyGridState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // 1. Estado para controlar o índice do filtro selecionado
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    // Chama loadMyListMovies() quando a tela é inicializada
    LaunchedEffect(Unit) {
        myListViewModel.loadMyListMovies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Lista") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VERMELHO,
                    titleContentColor = WHITE,
                    navigationIconContentColor = WHITE
                )
            )
        },
        floatingActionButton = {
            MaterialTheme(colorScheme = FabMenuColorScheme()) {
                val items =
                    listOf(
                        Icons.Outlined.AddComment to "Adicionar Filme",
                        Icons.Outlined.DeleteOutline to "Remover Filme"
                    )

                FloatingActionButtonMenu(
                    modifier = Modifier,
                    expanded = fabMenuExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            modifier =
                            Modifier
                                .semantics {
                                    traversalIndex = -1f
                                    stateDescription =
                                        if (fabMenuExpanded) "Expanded" else "Collapsed"
                                }
                                .animateFloatingActionButton(
                                    visible = fabVisible || fabMenuExpanded,
                                    alignment = Alignment.BottomEnd,
                                ),
                            checked = fabMenuExpanded,
                            onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                        ) {
                            val imageVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = null,
                                modifier = Modifier.animateIcon({ checkedProgress }),
                            )
                        }
                    },
                ) {
                    items.forEachIndexed { i, item ->
                        FloatingActionButtonMenuItem(
                            modifier =
                            Modifier.semantics {
                                isTraversalGroup = true
                                if (i == items.size - 1) {
                                    customActions =
                                        listOf(
                                            CustomAccessibilityAction(
                                                label = "Close menu",
                                                action = {
                                                    fabMenuExpanded = false
                                                    true
                                                },
                                            ),
                                        )
                                }
                            },
                            onClick = { fabMenuExpanded = false },
                            icon = { Icon(item.first, contentDescription = null) },
                            text = { Text(text = item.second) },
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BLACK)
        ) {
            // 2. Componente customizado sendo usado com o estado
            ConnectedButtonGroupComposableCustom(
                selectedIndex = selectedFilterIndex,
                onIndexChange = { newIndex ->
                    selectedFilterIndex = newIndex
                    // TODO: Adicionar a lógica de filtragens de Filmes Favoritos, Curtidos e de A-Z, vinda pela viewModel aqui.
                    // Ex: when (newIndex) {
                    //     0 -> viewModel.filterByFavorites()
                    //     1 -> viewModel.filterByLikes()
                    //     2 -> viewModel.sortByAlphabet()
                    // }
                }
            )

            when (val state = uiState) {
                is UiState.Idle -> {}
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicatorCustom(animationDelay = 400)
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
private fun MyListScreenPreview() {
    MyListScreen(onMovieClick = {}, onNavigateBack = {})
}