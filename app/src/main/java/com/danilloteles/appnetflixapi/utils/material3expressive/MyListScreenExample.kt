
package com.danilloteles.appnetflixapi.utils.material3expressive

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyListScreenExample() {
    val coroutineScope = rememberCoroutineScope()

    // State for the list of items
    val initialItems = listOf(
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

    // State for Pull-to-Refresh
    var isRefreshing by remember { mutableStateOf(false) }

    // State for the confirmation dialog
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            delay(2000) // Simulate network delay
            movieList.clear()
            movieList.addAll(initialItems.shuffled())
            isRefreshing = false
        }
    }

    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Netflix",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineMedium
                    ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, "Trigger Refresh", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            SmallExtendedFloatingActionButton(
                onClick = {
                    val newItem = "New Movie #${(1..100).random()}"
                    movieList.add(0, newItem)
                },
                expanded = expandedFab,
                icon = { Icon(Icons.Filled.Add, "Adicionar nova lista.") },
                text = { Text(text = "Adicionar Lista") },
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier.padding(paddingValues),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = movieList,
                    key = { it } // Use item as the key
                ) { item ->
                    DeletableListItem(
                        item = item,
                        onDeleteRequest = {
                            itemToDelete = it
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeletableListItem(
    item: String,
    onDeleteRequest: (String) -> Unit
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
            supportingContent = { Text("Swipe left to remove") },
            colors = ListItemDefaults.colors(
                containerColor = Color.Black,
                headlineColor = Color.White,
                supportingColor = Color.White
            )
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

@Preview
@Composable
fun MyListScreenExamplePreview() {
    MyListScreenExample()
}
