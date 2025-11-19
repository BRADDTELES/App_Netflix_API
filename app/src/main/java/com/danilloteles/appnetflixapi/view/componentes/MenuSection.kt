package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.R

@Composable
fun MenuSection(
    onMyListClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {

        val imagemFundo = R.drawable.capa

        AsyncImage(
            model = imagemFundo,
            contentDescription = "Capa do filme",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            NetflixNavegacaoRow(
                onMyListClick = onMyListClick
            )

            BotaoAssistir()
        }
    }
}

@Composable
fun NetflixNavegacaoRow(
    onMyListClick: () -> Unit = {}
) {

    val imagemLogo = R.drawable.logo

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = imagemLogo,
            contentDescription = "Logo da Netflix",
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        TextButtonSample(
            onClick = { /* TODO: Implementar navegação para séries */ },
            texto = "Séries"
        )

        Spacer(modifier = Modifier.width(16.dp))

        TextButtonSample(
            onClick = { /* TODO: Implementar navegação para filmes */ },
            texto = "Filmes"
        )

        Spacer(modifier = Modifier.width(16.dp))

        TextButtonSample(
            onClick = onMyListClick,
            texto = "Minha Lista"
        )
    }
}

@Composable
fun BotaoAssistir() {
    Button(
        onClick = {/* Ação do botão, não faz nada */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red
        ),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .background(
                color = Color.Red,
                shape = RoundedCornerShape(6.dp)
            )
    ) {

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "ASSISTIR",
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

    }
}

@Preview
@Composable
fun MenuSectionPreview() {
    MenuSection()
}