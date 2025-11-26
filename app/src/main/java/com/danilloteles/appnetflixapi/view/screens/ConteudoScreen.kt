@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.repository.MinhaListaRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.utils.events.SortOrder
import com.danilloteles.appnetflixapi.viewmodel.ConteudoViewModel

@Composable
fun ConteudoScreen(
    listId: String,
    movieTitle: String,
    onBackClick: () -> Unit,
    onMovieClick: (Int, String?) -> Unit,
    onSerieClick: (Int, String?) -> Unit
) {
    val context = LocalContext.current
    val viewModel: ConteudoViewModel = viewModel(
        key = listId,
        factory = ConteudoViewModel.ConteudoViewModelFactory(
            listId = listId,
            minhaListaRepository = MinhaListaRepository(
                filmeAPI = RetrofitHelper.filmeAPI,
                userPreferencesRepository = UserPreferencesRepository(context)
            ),
            userPreferencesRepository = UserPreferencesRepository(context)
        )
    )

    val lazyPagingItems: LazyPagingItems<MediaItem> = viewModel.conteudoPaginado.collectAsLazyPagingItems()
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = movieTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { lazyPagingItems.refresh() }) {
                        Icon(Icons.Filled.Refresh, "Atualizar", tint = Color.White)
                    }
                }
            )
        }
    ) {
paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ConnectedButtonGroupComposable(
                onSortChange = { newSortOrder -> viewModel.setSortOrder(newSortOrder) }
            )
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { lazyPagingItems.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = lazyPagingItems.loadState.refresh) {
                    is LoadState.Loading -> {
                        if (!isRefreshing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is LoadState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Erro: ${state.error.message}\nPuxe para tentar novamente.",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is LoadState.NotLoading -> {
                        if (lazyPagingItems.itemCount == 0) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Esta lista está vazia.", color = Color.White)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = lazyPagingItems.itemCount,
                                    key = { index -> lazyPagingItems.peek(index)?.id ?: index }
                                ) { index ->
                                    val item = lazyPagingItems[index]
                                    if (item != null) {
                                        VideoItem(
                                            item = item,
                                            listId = listId,
                                            onMovieClick = onMovieClick,
                                            onSerieClick = onSerieClick
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItem(
    item: MediaItem,
    listId: String,
    onMovieClick: (Int, String?) -> Unit,
    onSerieClick: (Int, String?) -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable {
                when (item.media_type) {
                    "movie" -> onMovieClick(item.id, listId)
                    "tv" -> onSerieClick(item.id, listId)
                }
            },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AsyncImage(
            model = "${Constantes.IMAGE_BASE_URL}${item.poster_path}",
            contentDescription = item.title,
            placeholder = painterResource(id = R.drawable.ic_placeholder),
            error = painterResource(id = R.drawable.ic_error),
            modifier = Modifier
                .width(160.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.FillHeight,
        )
        Text(
            text = item.title ?: item.name ?: "",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ConnectedButtonGroupComposable(onSortChange: (SortOrder) -> Unit) {
    val options = listOf("Padrão", "A-Z")
    val unCheckedIcons =
        listOf(Icons.Filled.FavoriteBorder, Icons.Default.Abc)
    val checkedIcons = listOf(Icons.Filled.Favorite, Icons.Filled.Abc)
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val modifiers = listOf(
            Modifier.weight(1f),
            Modifier.weight(1f),
        )

        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = {
                    selectedIndex = index
                    val newSortOrder = when (index) {
                        0 -> SortOrder.DEFAULT
                        1 -> SortOrder.TITLE_ASC
                        else -> SortOrder.DEFAULT // Fallback, embora não deva ser alcançado
                    }
                    onSortChange(newSortOrder)
                },
                modifier = modifiers[index].semantics { role = Role.RadioButton },
                shapes =
                when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes() // Não deve ser alcançado com 2 botões
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = Color.Red,
                    checkedContentColor = Color.White,
                    containerColor = Color.White,
                    contentColor = Color.Gray
                )
            ) {
                Icon(
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = label,
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

@Preview
@Composable
private fun ConteudoScreenPreview() {
    // O Preview depende de um ViewModel funcional e dados reais.
    // Para um preview útil, seria necessário injetar uma implementação falsa
    // que retorna PagingData estático.
    ConteudoScreen(
        listId = "1",
        movieTitle = "Minha Lista de Exemplo",
        onBackClick = {},
        onMovieClick = { _, _ -> },
        onSerieClick = { _, _ -> }
    )
}
