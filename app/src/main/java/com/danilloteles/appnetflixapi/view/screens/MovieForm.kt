package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.view.componentes.OutlinedTextFieldCustom

@Composable
fun MovieForm(
    movie: Movie
) {

    var título by remember { mutableStateOf(movie.title) }

    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BLACK)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 3f)
            ) {
                AsyncImage(
                    model = movie.imagemUrl,
                    contentDescription = "Imagem da capa do filme",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = {
                        /* TODO: Ação para abrir a Galeria do celular */
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(
                            color = TRANSPARENT
                        )
                        .border(
                            width = 2.dp,
                            color = WHITE,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "Abrir a Galeria",
                        tint = WHITE
                    )
                }
            }

            OutlinedTextFieldCustom(
                value = título,
                onValueChange = { título = it },
                label = "Título",
                modifier = Modifier.fillMaxWidth().padding(20.dp, 32.dp),
                keyboardOptions  = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
            Button(
                onClick = {
                    /* Ação do botão salvar ou editar o filme */
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(6.dp)
                    )
            ) {

                Icon(
                    imageVector = Icons.Default.SaveAs,
                    contentDescription = "Play",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SALVAR",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

            }
        }
    }
}

@Preview
@Composable
private fun MovieFormPreview(){
    MovieForm(
        movie = Movie(
            id = 1,
            title = "Filme",
            imagemUrl = R.drawable.capa
        )
    )
}