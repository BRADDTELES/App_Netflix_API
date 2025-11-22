@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.danilloteles.appnetflixapi.view.screens

import android.util.Log
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.datasource.datastore.MyListPreferencesRepository
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.repository.FilmeRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.MovieListFilterState
import com.danilloteles.appnetflixapi.utils.material3expressive.ConnectedButtonGroupComposableCustom
import com.danilloteles.appnetflixapi.utils.material3expressive.FabMenuColorScheme
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.componentes.MyMoviesListSection
import com.danilloteles.appnetflixapi.viewmodel.MyListViewModel

@Composable
fun MyListScreen(
    onMovieClick: (MediaItem, String?) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToListForm: () -> Unit
) {
    val context = LocalContext.current // Adicionado para o ViewModelFactory

    val filmeRepository = remember {
        FilmeRepository(RetrofitHelper.filmeAPI)
    }

    val myListViewModel: MyListViewModel = viewModel( // Instância alterada
        factory = MyListViewModel.MyListViewModelFactory(
            UserPreferencesRepository(context),
            MyListPreferencesRepository(context),
            filmeRepository = filmeRepository
        )
    )

    val moviesPagingItems = myListViewModel.moviesStream.collectAsLazyPagingItems()

    val userListsUiState by myListViewModel.userListsUiState.collectAsStateWithLifecycle()

    val listState = rememberLazyGridState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para controlar o dropdown de listas
    var expanded by remember { mutableStateOf(false) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListName by rememberSaveable { mutableStateOf("Minhas Listas") }

    val currentFilter by myListViewModel.currentFilter.collectAsStateWithLifecycle()

    // 1. Estado para controlar o índice do filtro selecionado
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(userListsUiState) {
        if (userListsUiState is UiState.Success) {
            val lists = (userListsUiState as UiState.Success).data
            val primaryList = lists.firstOrNull { it.name == "Minha Lista" } ?: lists.firstOrNull()
            if (primaryList != null) {
                selectedListId = primaryList.id.toString()
                selectedListName = primaryList.name
                myListViewModel.applyFilter(MovieListFilterState.MyList(primaryList.id.toString()))
            } else {
                // Se não encontrou nenhuma lista, carrega filmes em cartaz por padrão
                selectedListId = "now_playing_movies" // ID especial para filmes em cartaz
                selectedListName = "Filmes em Cartaz"
                myListViewModel.applyFilter(MovieListFilterState.NowPlaying)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        Text(
                            text = selectedListName,
                            modifier = Modifier.menuAnchor(),
                            color = WHITE
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            when (val state = userListsUiState) {
                                is UiState.Success -> {
                                    DropdownMenuItem(
                                        text = { Text("Filmes em Cartaz") },
                                        onClick = {
                                            selectedListId = "now_playing_movies"
                                            selectedListName = "Filmes em Cartaz"
                                            myListViewModel.applyFilter(MovieListFilterState.NowPlaying)
                                            expanded = false
                                        },
                                        // Opcional: Adicionar um ícone para filmes em cartaz
                                    )
                                    state.data.forEach { list ->
                                        DropdownMenuItem(
                                            text = { Text(list.name) },
                                            onClick = {
                                                selectedListId = list.id.toString()
                                                selectedListName = list.name
                                                myListViewModel.applyFilter(MovieListFilterState.MyList(list.id.toString()))
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                                is UiState.Loading -> {
                                    DropdownMenuItem(
                                        text = { Text("Carregando listas...") },
                                        onClick = { /* No-op */ }
                                    )
                                }
                                is UiState.Error -> {
                                    DropdownMenuItem(
                                        text = { Text("Erro ao carregar listas") },
                                        onClick = { /* No-op */ }
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("TAG-MyListScreen", "Botão de voltar clicado.")
                        onNavigateBack()
                    }) {
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
                        Icons.Default.Add to "Criar Nova Lista"
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
                            onClick = {
                                if (item.second == "Criar Nova Lista") {
                                    onNavigateToListForm()
                                }
                                fabMenuExpanded = false
                                      },
                            icon = { Icon(item.first, contentDescription = null) },
                            text = { Text(text = item.second) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BLACK)
        ) {
            // 2. Componente customizado sendo usado com o estado
            ConnectedButtonGroupComposableCustom(
                selectedIndex = when (currentFilter) {
                    is MovieListFilterState.MyList -> 0
                    MovieListFilterState.Popular -> 1
                    MovieListFilterState.TopRated -> 2
                    MovieListFilterState.NowPlaying -> 3
                },
                onIndexChange = { newIndex ->
                    val newFilter = when (newIndex) {
                        0 -> MovieListFilterState.MyList(selectedListId)
                        1 -> MovieListFilterState.Popular
                        2 -> MovieListFilterState.TopRated
                        3 -> MovieListFilterState.NowPlaying
                        else -> MovieListFilterState.Popular
                    }
                    myListViewModel.applyFilter(newFilter)
                }
            )

            Box(
                modifier = Modifier.fillMaxSize().background(BLACK),
                contentAlignment = Alignment.Center
            ) {
                if (moviesPagingItems.loadState.refresh is LoadState.Loading) {
                    LoadingIndicatorCustom(animationDelay = 2000)
                } else if (moviesPagingItems.loadState.refresh is LoadState.Error) {
                    val error = moviesPagingItems.loadState.refresh as LoadState.Error
                    Text(
                        text = "Erro ao carregar itens.",
                        color = WHITE
                    )
                    Log.e("TAG-MyListScreen","Erro ao carregar filmes: ${error.error.localizedMessage}")
                } else if (moviesPagingItems.itemCount == 0){
                    Text(
                        text = "Nenhum item encontrado.",
                        color = WHITE
                    )
                    Log.e("TAG-MyListScreen","Nenhum filme encontrado.")
                } else {
                    MyMoviesListSection(
                        items = moviesPagingItems,
                        onMovieClick = { mediaItem -> onMovieClick(mediaItem, selectedListId) },
                        lazyGridState = listState
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MyListScreenPreview() {
    MyListScreen(onMovieClick = { _, _ -> }, onNavigateBack = {}, onNavigateToListForm = {})
}