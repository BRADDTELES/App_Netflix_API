package com.danilloteles.appnetflixapi.view.componentes

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
import com.danilloteles.appnetflixapi.model.Movie

@Composable
fun MovieItem(
    movie: Movie,
    onMovieClick: (Movie) -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable{ onMovieClick(movie) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(movie.imagemUrl)
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            modifier = Modifier
                .width(160.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.FillHeight,
            placeholder = painterResource( R.drawable.ic_placeholder),
            error = painterResource(R.drawable.ic_error)
        )

        Text(
            text = movie.title,
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
        movie = Movie(
            id = 1,
            title = "Filme",
            imagemUrl = R.drawable.capa
        ),
        onMovieClick = {}
    )
}