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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.danilloteles.appnetflixapi.utils.MyListPreferencesRepository
import android.util.Log // Import adicionado
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon

@Composable
fun MyListScreen(
    onMovieClick: (Filme) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToListForm: () -> Unit
) {
    val context = LocalContext.current // Adicionado para o ViewModelFactory
    val myListViewModel: MyListViewModel = viewModel( // Instância alterada
        factory = MyListViewModel.MyListViewModelFactory(
            UserPreferencesRepository(context),
            MyListPreferencesRepository(context)
        )
    )
    val uiState by myListViewModel.uiState.collectAsStateWithLifecycle()
    val userListsUiState by myListViewModel.userListsUiState.collectAsStateWithLifecycle()
    val listState = rememberLazyGridState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para controlar o dropdown de listas
    var expanded by remember { mutableStateOf(false) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListName by rememberSaveable { mutableStateOf("Minhas Listas") }

    // 1. Estado para controlar o índice do filtro selecionado
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    // Chama loadMyListMovies() quando a tela é inicializada
    LaunchedEffect(Unit) {
        myListViewModel.loadMyListMovies() // Carrega a lista padrão ao iniciar
    }

    LaunchedEffect(userListsUiState) {
        if (userListsUiState is UiState.Success && selectedListId == null) {
            val lists = (userListsUiState as UiState.Success).data
            // Tenta selecionar a lista "Minha Lista" por padrão ou a primeira lista
            val primaryList = lists.firstOrNull { it.name == "Minha Lista" } ?: lists.firstOrNull()
            primaryList?.let {
                selectedListId = it.id.toString()
                selectedListName = it.name
                myListViewModel.loadMyListMovies(it.id.toString())
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
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
                                    state.data.forEach { list ->
                                        DropdownMenuItem(
                                            text = { Text(list.name) },
                                            onClick = {
                                                selectedListId = list.id.toString()
                                                selectedListName = list.name
                                                myListViewModel.loadMyListMovies(list.id.toString())
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
                        Icons.Default.Add to "Criar Nova Lista",
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
                selectedIndex = selectedFilterIndex,
                onIndexChange = { newIndex ->
                    selectedFilterIndex = newIndex
                    myListViewModel.applyFilter(newIndex)
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
                    if (state.data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sua lista está vazia.",
                                color = WHITE
                            )
                        }
                    } else {
                        PopularMoviesSection(
                            listFilme = state.data,
                            onMovieClick = onMovieClick,
                            lazyGridState = listState
                        )
                    }
                }
                is UiState.Error -> {
                    // O erro agora é tratado pelo Snackbar.
                    // Opcionalmente, pode-se manter o estado visual anterior ou um estado vazio.
                    // Para simplificar, exibimos o mesmo que a lista vazia.
                    if ((uiState as? UiState.Success)?.data.isNullOrEmpty() == true) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sua lista está vazia.",
                                color = WHITE
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MyListScreenPreview() {
    MyListScreen(onMovieClick = {}, onNavigateBack = {}, onNavigateToListForm = {})
}