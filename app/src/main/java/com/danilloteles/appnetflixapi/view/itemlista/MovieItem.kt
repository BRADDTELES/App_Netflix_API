package com.danilloteles.appnetflixapi.view.itemlista

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.model.Filme

@Composable
fun MovieItem(
    filme: Filme,
    onMovieClick: (Filme) -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable{ onMovieClick(filme) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Constantes.IMAGE_BASE_URL + filme.poster_path)
                .crossfade(true)
                .build(),
            contentDescription = filme.title,
            modifier = Modifier
                .width(160.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.FillHeight,
            placeholder = painterResource( R.drawable.ic_placeholder),
            error = painterResource(R.drawable.capa)
        )

        Text(
            text = filme.title,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 8.dp),
            textAlign = TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

    }

}

@Preview
@Composable
fun MovieItemPreview() {
    MovieItem(
        filme = Filme(
            id = 1,
            title = "Filme de Teste",
            poster_path = "/t6HIqrRAFyUMC6bZqMfPSzPNw0s.jpg",
            adult = false,
            backdrop_path = "",
            original_language = "en-US",
            original_title = "Test Movie",
            overview = "This is a test movie for preview.",
            popularity = 100.0,
            release_date = "2025-11-16",
            video = false,
            vote_average = 7.0,
            vote_count = 100,
            genre_ids = emptyList(),
        ),
        onMovieClick = {}
    )
}