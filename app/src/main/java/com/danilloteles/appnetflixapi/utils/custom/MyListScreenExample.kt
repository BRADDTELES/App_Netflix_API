@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)

package com.danilloteles.appnetflixapi.utils.custom

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun MyListScreenExample() {

    val coroutineScope = rememberCoroutineScope()

    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<MovieNavItemData>()
    val selectedMovie = scaffoldNavigator.currentDestination?.contentKey

    // State for the list of items
    val initialItems = listOf(
        "Lista de filmes da infância 30",
        "Minhas séries favoritas",
        "Listagem de filmes",
        "Séries marcantes",
        "Stranger Things",
        "The Witcher",
        "The Crown",
        "Bridgerton",
        "Money Heist",
        "Ozark",
        "Cobra Kai",
        "The Queen's Gambit",
        "Breaking Bad",
        "Game of Thrones",
        "Chernobyl",
        "Black Mirror",
        "La Casa de Papel",
        "Grey's Anatomy",
        "The Big Bang Theory",
        "Dexter",
        "Hannibal",
        "Homeland",
        "Narcos",
        "Crazy Ex-Girlfriend",
    )
    val movieList = remember { mutableStateListOf(*initialItems.toTypedArray()) }

    // State for the confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

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
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Netflix",
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
                                    movieList.clear()
                                    movieList.addAll(initialItems.shuffled())
                                }) {
                                    Icon(Icons.Filled.Refresh, "Atualizar", tint = Color.White)
                                }
                            }
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
                                            if (item.second == "Criar Nova Lista") {}
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
                    PullToRefreshBox( // Mantendo o PullToRefreshBox para a lista principal
                        modifier = Modifier.padding(paddingValues),
                        isRefreshing = false, // Não é mais gerenciado aqui, mas o componente exige
                        onRefresh = { /* No-op, pois o refresh agora está no botão da TopAppBar */ },
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = movieList,
                                key = { _, item -> item }
                            ) { index, item ->
                                DeletableListItem(
                                    item = item,
                                    onDeleteRequest = {
                                        itemToDelete = it
                                        showDeleteConfirmation = true
                                    },
                                    onItemClick = { clickedItem ->
                                        coroutineScope.launch {
                                            scaffoldNavigator.navigateTo(
                                                pane = ListDetailPaneScaffoldRole.Detail,
                                                contentKey = MovieNavItemData(index, clickedItem)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                selectedMovie?.let {
                    MyVideosScreenExample(
                        movieTitle = it.movieTitle,
                        onRefreshRequest = { /* A lógica de refresh já está interna ao MyVideosScreen */ },
                        onBackClick = { coroutineScope.launch { scaffoldNavigator.navigateBack() } }
                    )
                }
            }
        },
    )

    if (showDeleteConfirmation && itemToDelete != null) {
        ConfirmationAlertDialog(
            itemName = itemToDelete!!,
            onConfirm = {
                movieList.remove(itemToDelete)
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
    item: String,
    onDeleteRequest: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.LightGray
                    SwipeToDismissBoxValue.StartToEnd -> Color.Transparent // No action on this side
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }, label = "background color"
            )
            Box(Modifier.fillMaxSize().background(color))
        },
        onDismiss = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest(item)
            }
            // Always reset the state to show the item again, dialog handles the action
            scope.launch {
                dismissState.reset()
            }
        },
    ) {
        ListItem(
            headlineContent = { Text(item) },
            supportingContent = { Text(text = "Deslize a esquerda para remover.", fontSize = 12.sp) },
            colors = ListItemDefaults.colors(
                containerColor = Color.Black,
                headlineColor = Color.White,
                supportingColor = Color.White
            ),
            modifier = Modifier.clickable { onItemClick(item) } // Adicionado o clickable aqui
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
fun FabMenuColorScheme(): ColorScheme {
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
fun MyListScreenExamplePreview() {
    MyListScreenExample()
}
