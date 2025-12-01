package com.danilloteles.appnetflixapi.utils.custom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@Composable
fun AnimatedExtendedFab(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        expanded = expanded,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Ícone de Adicionar"
            )
        },
        text = {
            Text(text = "Adicionar Filme")
        },
        containerColor = VERMELHO,
        contentColor = WHITE,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AnimatedExtendedFabPreviewExpanded(){
    AnimatedExtendedFab(expanded = true, onClick = {})
}

@Preview(showBackground = true)
@Composable
private fun AnimatedExtendedFabPreviewCollapsed(){
    AnimatedExtendedFab(expanded = false, onClick = {})
}