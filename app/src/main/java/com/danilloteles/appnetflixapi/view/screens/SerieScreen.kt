@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.danilloteles.appnetflixapi.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonGroupDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.repository.v3.SerieRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.GRAY
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.SerieListFilterState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.itemlista.SerieItem
import com.danilloteles.appnetflixapi.viewmodel.SerieViewModel

@Composable
fun SerieScreen(
    onSerieClick: (MediaItem) -> Unit,
) {

    val context = LocalContext.current

    val serieRepository = remember {
        SerieRepository(RetrofitHelper.filmeAPI)
    }

    val serieViewModel: SerieViewModel = viewModel(
        factory = SerieViewModel.SerieListModelFactory(
            serieRepository = serieRepository
        )
    )

    val seriesPagingItems = serieViewModel.seriesStream.collectAsLazyPagingItems()
    val currentFilter by serieViewModel.currentFilter.collectAsStateWithLifecycle()
    val isRefreshing = seriesPagingItems.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Séries",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VERMELHO,
                    actionIconContentColor = WHITE,
                    titleContentColor = WHITE
                ),
                actions = {
                    IconButton(onClick = { seriesPagingItems.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = WHITE)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BLACK)
        ) {
            ConnectedButtonGroupComposableSerieCustom(
                selectedIndex = when (currentFilter) {
                    is SerieListFilterState.Popular -> 0
                    is SerieListFilterState.TopRated -> 1
                },
                onIndexChange = { newIndex ->
                    val newFilter = when (newIndex) {
                        0 -> SerieListFilterState.Popular
                        1 -> SerieListFilterState.TopRated
                        else -> SerieListFilterState.Popular
                    }
                    serieViewModel.applyFilter(newFilter)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { seriesPagingItems.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (seriesPagingItems.loadState.refresh) {
                        is LoadState.Loading -> {
                            LoadingIndicatorCustom(animationDelay = 2000)
                        }
                        is LoadState.Error -> {
                            val error = seriesPagingItems.loadState.refresh as LoadState.Error
                            Text(
                                text = "Erro ao carregar séries.",
                                color = WHITE,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            Log.e("TAG-SerieListScreen","Erro ao carregar séries: ${error.error.localizedMessage}")
                            Log.e("TAG-SerieListScreen","Erro: ${error.error}")
                        }
                        else -> {
                            if (seriesPagingItems.itemCount == 0) {
                                Text(text = "Nenhuma série encontrada.", color = WHITE)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    items(
                                        count = seriesPagingItems.itemCount,
                                        key = seriesPagingItems.itemKey { it.id }
                                    ) { index ->
                                        val serie = seriesPagingItems[index]
                                        if (serie != null) {
                                            SerieItem(
                                                serie = serie,
                                                onClick = { onSerieClick(serie) }
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
}

@Composable
fun ConnectedButtonGroupComposableSerieCustom(
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Popular", "Top Séries")
    val unCheckedIcons = listOf(Icons.Outlined.ThumbUp, Icons.Outlined.StarOutline)
    val checkedIcons = listOf(Icons.Filled.ThumbUp, Icons.Filled.Star)

    Row(
        modifier = modifier.padding(start = 30.dp, end = 30.dp, top = 16.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {

        val modifiers = listOf(
            Modifier.weight(1f),
            Modifier.weight(1f),
        )

        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onIndexChange(index) },
                modifier = modifiers[index].semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = VERMELHO,
                    checkedContentColor = WHITE,
                    containerColor = WHITE,
                    contentColor = GRAY
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
fun SerieScreenPreview() {
    SerieScreen(
        onSerieClick = {},
    )
}