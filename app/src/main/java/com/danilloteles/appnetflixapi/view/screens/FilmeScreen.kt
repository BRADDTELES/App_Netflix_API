@file:OptIn(ExperimentalMaterial3Api::class)

package com.danilloteles.appnetflixapi.view.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.repository.FilmeRepository
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.FilmeListFilterState
import com.danilloteles.appnetflixapi.utils.custom.ConnectedButtonGroupComposableSerieCustom
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.view.itemlista.MovieItem
import com.danilloteles.appnetflixapi.viewmodel.FilmeViewModel

@Composable
fun FilmeScreen(
    onFilmeClick: (MediaItem) -> Unit
) {

    val filmeRepository = remember {
        FilmeRepository(RetrofitHelper.filmeAPI)
    }

    val filmeViewModel: FilmeViewModel = viewModel(
        factory = FilmeViewModel.FilmeListModelFactory(
            filmeRepository = filmeRepository
        )
    )

    val filmesPagingItems = filmeViewModel.filmesStream.collectAsLazyPagingItems()
    val currentFilter by filmeViewModel.currentFilter.collectAsStateWithLifecycle()
    val isRefreshing = filmesPagingItems.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Filmes",
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
                    IconButton(onClick = { filmesPagingItems.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", tint = WHITE)
                    }
                }
            )
        }
    ){ paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BLACK)
        ) {
            ConnectedButtonGroupComposableSerieCustom(
                selectedIndex = when (currentFilter) {
                    is FilmeListFilterState.Popular -> 0
                    is FilmeListFilterState.TopRated -> 1
                },
                onIndexChange = { newIndex ->
                    val newFilter = when (newIndex) {
                        0 -> FilmeListFilterState.Popular
                        1 -> FilmeListFilterState.TopRated
                        else -> FilmeListFilterState.Popular
                    }
                    filmeViewModel.applyFilter(newFilter)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { filmesPagingItems.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (filmesPagingItems.loadState.refresh) {
                        is LoadState.Loading -> {
                            LoadingIndicatorCustom(animationDelay = 2000)
                        }
                        is LoadState.Error -> {
                            val error = filmesPagingItems.loadState.refresh as LoadState.Error
                            Text(
                                text = "Erro ao carregar filmes.",
                                color = WHITE,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            Log.e("TAG-FilmeListScreen","Erro ao carregar filmes: ${error.error.localizedMessage}")
                            Log.e("TAG-FilmeListScreen","Erro: ${error.error}")
                        }
                        else -> {
                            if (filmesPagingItems.itemCount == 0) {
                                Text(text = "Nenhuma filme encontrada.", color = WHITE)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    items(
                                        count = filmesPagingItems.itemCount,
                                        key = filmesPagingItems.itemKey { it.id }
                                    ) { index ->
                                        val mediaItem = filmesPagingItems[index]
                                        if (mediaItem != null) {
                                            MovieItem(
                                                filme = mediaItem,
                                                onMovieClick = { onFilmeClick(mediaItem) }
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

@Preview
@Composable
fun FilmeScreenPreview() {
    FilmeScreen(
        onFilmeClick = {},
    )
}