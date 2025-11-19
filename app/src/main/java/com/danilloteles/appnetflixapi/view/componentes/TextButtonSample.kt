package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.danilloteles.appnetflixapi.ui.theme.WHITE


@Composable
fun TextButtonSample(
    onClick: () -> Unit,
    texto: String
) {
    TextButton(
        onClick = onClick
    ) {
        Text(
            text = texto,
            color = WHITE,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
fun TextButtonSamplePreview() {
    TextButtonSample(
        onClick = {},
        texto = ""
    )
}