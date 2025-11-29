package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.danilloteles.appnetflixapi.model.v3.MediaItem
import com.danilloteles.appnetflixapi.view.itemlista.MovieItem

@Composable
fun PopularMoviesSection(
    filmesPaginados: LazyPagingItems<MediaItem>,
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
            text = "Populares",
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
            items(filmesPaginados.itemCount) { index ->
                val filme = filmesPaginados[index]
                if (filme != null) {
                    MovieItem(
                        filme = filme,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

    }
}
