@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.runtime.Composable
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.filme.TmdbList
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.viewmodel.MinhaListaViewModel
import kotlinx.coroutines.launch

@Composable
fun MinhaListaScreen(
    onNavigateToConteudo: (listId: String, listName: String) -> Unit,
    onNavigateToFormulario: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MinhaListaViewModel = viewModel(
        factory = MinhaListaViewModel.MinhaListaViewModelFactory(
            userPreferencesRepository = UserPreferencesRepository(context),
            minhaListaRepository = MinhaListaRepository(
                filmeAPI = RetrofitHelper.filmeAPI,
                userPreferencesRepository = UserPreferencesRepository(context)
            ),
            filmeAPI = RetrofitHelper.filmeAPI
        )
    )

    val uiState by viewModel.minhasListasState.collectAsStateWithLifecycle()

    val isRefreshing = uiState is UiState.Loading

    val coroutineScope = rememberCoroutineScope()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<MovieNavItemData>()

    val selectedItem = scaffoldNavigator.currentDestination?.contentKey

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<TmdbList?>(null) }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        scaffoldState = scaffoldNavigator.scaffoldState,
        listPane = {
            AnimatedPane {
                Scaffold(
                    containerColor = BLACK,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Minha Lista",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Red,
                                actionIconContentColor = Color.White,
                                titleContentColor = Color.White
                            ),
                            actions = {
                                IconButton(onClick = {
                                    viewModel.buscarMinhasListas()
                                }) {
                                    Icon(Icons.Filled.Refresh, "Atualizar", tint = Color.White)
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        MaterialTheme(colorScheme = fabMenuColorScheme()) {
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
                                                onNavigateToFormulario()
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
                ) { paddingValues ->
                    PullToRefreshBox(
                        modifier = Modifier.padding(paddingValues),
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.buscarMinhasListas() },
                    ) {
                        when (val state = uiState) {
                            is UiState.Loading -> {
                                if (!isRefreshing) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            is UiState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Erro: ${state.message}\nPuxe para tentar novamente.",
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            is UiState.Success -> {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(
                                        items = state.data,
                                        key = { tmdbList -> tmdbList.id }
                                    ) { tmdbList ->
                                        DeletableListItem(
                                            item = tmdbList,
                                            onDeleteRequest = {
                                                itemToDelete = it
                                                showDeleteConfirmation = true
                                            },
                                            onItemClick = { clickedItem ->
                                                coroutineScope.launch {
                                                    scaffoldNavigator.navigateTo(
                                                        pane = ListDetailPaneScaffoldRole.Detail,
                                                        contentKey = MovieNavItemData(clickedItem.id, clickedItem.name)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            is UiState.Idle -> {
                                Box(modifier = Modifier.fillMaxSize()) {}
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                selectedItem?.let {
                    ConteudoScreen(
                        movieTitle = it.movieTitle,
                        onBackClick = { coroutineScope.launch { scaffoldNavigator.navigateBack() } }
                    )
                }
            }
        },
    )

    if (showDeleteConfirmation && itemToDelete != null) {
        ConfirmationAlertDialog(
            itemName = itemToDelete!!.name,
            onConfirm = {
                // TODO: Aqui entraria a lógica para deletar a lista via ViewModel
                // viewModel.deleteList(itemToDelete!!.id)
                showDeleteConfirmation = false
                itemToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                itemToDelete = null
            }
        )
    }
}

@Composable
private fun DeletableListItem(
    item: TmdbList,
    onDeleteRequest: (TmdbList) -> Unit,
    onItemClick: (TmdbList) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.LightGray
                    SwipeToDismissBoxValue.StartToEnd -> Color.Transparent
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }, label = "background color"
            )
            Box(Modifier.fillMaxSize().background(color))
        },
        onDismiss = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest(item)
            }
            scope.launch {
                dismissState.reset()
            }
        },
    ) {
        ListItem(
            headlineContent = { Text(item.name) },
            supportingContent = { Text(text = "${item.item_count} conteúdos. Deslize para a esquerda para remover.", fontSize = 12.sp) },
            colors = ListItemDefaults.colors(
                containerColor = Color.Black,
                headlineColor = Color.White,
                supportingColor = Color.White
            ),
            modifier = Modifier.clickable { onItemClick(item) }
        )
    }
}

@Composable
private fun ConfirmationAlertDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = itemName) },
        text = { Text(text = "Deseja remover este item da sua Lista?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Remover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

data class MovieNavItemData(val index: Int, val movieTitle: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(index)
        parcel.writeString(movieTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MovieNavItemData> {
        override fun createFromParcel(parcel: Parcel): MovieNavItemData {
            return MovieNavItemData(parcel)
        }

        override fun newArray(size: Int): Array<MovieNavItemData?> {
            return arrayOfNulls(size)
        }
    }
}

@Composable
fun fabMenuColorScheme(): ColorScheme {
    // Copia o tema atual e sobrescreve apenas as cores desejadas
    return MaterialTheme.colorScheme.copy(

        primary = Color.Red,

        // Usado pelo FAB principal (collapsed) e itens do menu
        primaryContainer = Color(0xFFFFEBEE),
        onPrimaryContainer = Color.Black,

        // Usado pelo FAB principal (expanded)
        secondaryContainer = Color.Red,
        onSecondaryContainer = Color.White,

        // Usado pelo texto (label) dos itens do menu
        onSurface = Color.Black
    )
}

@Preview
@Composable
private fun MinhaListaScreenPreview(){
    MinhaListaScreen(
        onNavigateToConteudo = { _, _ -> },
        onNavigateToFormulario = {}
    )
}