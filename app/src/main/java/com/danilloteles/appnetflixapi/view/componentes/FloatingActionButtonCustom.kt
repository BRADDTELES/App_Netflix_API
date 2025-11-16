package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@Composable
fun FloatingActionButtonCustom(
    onAddClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onAddClick,
        containerColor = VERMELHO,
        shape = RoundedCornerShape(32.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Botão de adicionar",
            tint = WHITE
        )
    }
}

@Preview
@Composable
private fun FloatingActionButtonCustomPreview(){
    FloatingActionButtonCustom(
        onAddClick = {}
    )
}