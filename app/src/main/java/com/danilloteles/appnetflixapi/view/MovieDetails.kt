package com.danilloteles.appnetflixapi.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.GRAY
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@Composable
fun MovieDetails(
    movie: Movie
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
        ) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .background(BLACK)
            ) {

                val imagemCapa = R.drawable.capa

                AsyncImage(
                    model = imagemCapa,
                    contentDescription = "Imagem da capa do filme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BotaoEditar()
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
            text = "Título",
            color = WHITE,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Descrição",
            color = WHITE,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp),
            maxLines = 5
        )
    }
}

@Composable
fun BotaoEditar() {
    Button(
        onClick = {
            /* Ação do botão de editar */
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = TRANSPARENT
        ),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .background(
                color = TRANSPARENT,
                shape = RoundedCornerShape(12.dp)
            )
            .size(72.dp)
            .border(
                width = 1.dp,
                color = WHITE,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
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
            imagemUrl = R.drawable.capa
        )
    )
}