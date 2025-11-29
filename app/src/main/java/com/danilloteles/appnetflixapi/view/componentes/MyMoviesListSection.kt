package com.danilloteles.appnetflixapi.view.componentes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.view.itemlista.MovieItem
import com.danilloteles.appnetflixapi.view.itemlista.SerieItem

@Composable
fun MyMoviesListSection(
    items: LazyPagingItems<MediaItem>,
    onMovieClick: (MediaItem) -> Unit,
    lazyGridState: LazyGridState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        Text(
            text = "Minha Lista",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            items(items.itemCount, key = { index -> items[index]?.id ?: index }) { index ->
                val item = items[index]
                if (item != null) {
                    Log.d("TAG-MyMoviesListSection", "Item no índice $index: id=${item.id}, media_type=${item.media_type}, title=${item.title}, name=${item.name}")
                    when (item.media_type) {
                        "movie" -> {
                            MovieItem(
                                filme = item,
                                onMovieClick = onMovieClick
                            )
                        }
                        "tv" -> {
                            SerieItem(
                                serie = item,
                                onClick = onMovieClick
                            )
                        }
                        else -> {
                            // Opcional: Um item para tipos desconhecidos ou erro
                        }
                    }
                }
            }

            items.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().background(BLACK).padding(16.dp)) {
                                LoadingIndicatorCustom(animationDelay = 1000, modifier = Modifier.align(
                                    Alignment.Center))
                            }
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item {
                            Text(
                                text = "Erro ao carregar mais itens.",
                                color = VERMELHO,
                                modifier = Modifier.padding(8.dp)
                            )
                            Log.e("TAG-MyMoviesListSection","Erro ao carregar mais filmes: ${error.error.localizedMessage}")
                        }
                    }
                }
            }
        }
    }
}