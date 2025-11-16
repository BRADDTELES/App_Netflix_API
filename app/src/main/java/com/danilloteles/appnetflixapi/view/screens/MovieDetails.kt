package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.ModeEditOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar

@Composable
fun MovieDetails(
    movie: Movie,
    onEditClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BLACK)
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                AsyncImage(
                    model = movie.imagemUrl,
                    contentDescription = "Imagem da capa do filme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f / 2f),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BotaoEditar(onClick = { onEditClick(movie.id) })
                }
            }
            ConteudoFilme(movie = movie)
        }
    }
}

@Composable
fun ConteudoFilme(
    movie: Movie
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BLACK)
            .padding(16.dp)
    ) {
        Text(
            text = movie.title,
            color = WHITE,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Descrição muito longa, demostrando todo os detalhes que o breve resume é contado no filme.",
            color = WHITE,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 5
        )
    }
}

@Composable
fun BotaoEditar(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                color = TRANSPARENT
            )
            .size(60.dp)
            .border(
                width = 2.dp,
                color = WHITE,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Botão de editar",
            tint = WHITE
        )
    }
}

@Preview
@Composable
private fun MovieDetailsPreview() {
    MovieDetails(
        movie = Movie(
            id = 1,
            title = "Filme",
            imagemUrl = R.drawable.movie_inception
        ),
        onEditClick = {}
    )
}