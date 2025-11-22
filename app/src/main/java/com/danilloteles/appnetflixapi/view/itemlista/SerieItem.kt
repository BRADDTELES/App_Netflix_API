package com.danilloteles.appnetflixapi.view.itemlista

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.danilloteles.appnetflixapi.model.MediaItem
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@Composable
fun SerieItem(
    serie: MediaItem,
    onClick: (MediaItem) -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable{ onClick(serie) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Constantes.IMAGE_BASE_URL + serie.poster_path)
                .crossfade(true)
                .build(),
            contentDescription = serie.name,
            modifier = Modifier
                .width(160.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.FillHeight,
            placeholder = painterResource(id = R.drawable.capa),
            error = painterResource(id = R.drawable.capa)
        )
        Text(
            text = serie.name ?: serie.title ?: "",
            color = WHITE,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun SerieItemPreview() {
    SerieItem(
        serie = MediaItem(
            id = 1,
            name = "Título da Série",
            poster_path = "/t6HIqrRAFyUMC6bZqMfPSzPNw0s.jpg",
            backdrop_path = "",
            overview = "Esta é uma série de teste para preview.",
            popularity = 100.0,
            vote_average = 7.0,
            vote_count = 100,
            media_type = "tv",
            first_air_date = "2025-11-16",
            original_name = "Original Series Title",
            origin_country = listOf("US"),
            title = null,
            original_title = null,
            release_date = null,
            adult = null,
            video = null
        ),
        onClick = {}
    )
}