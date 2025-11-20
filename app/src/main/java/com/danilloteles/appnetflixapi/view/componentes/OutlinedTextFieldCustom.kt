package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO

@Composable
fun OutlinedTextFieldCustom(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        keyboardOptions = keyboardOptions,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            cursorColor = VERMELHO, // Cor do cursor
            focusedIndicatorColor = VERMELHO,
            unfocusedIndicatorColor = VERMELHO,
            focusedContainerColor = TRANSPARENT,
            unfocusedContainerColor = TRANSPARENT,
            focusedTextColor = VERMELHO,
            unfocusedTextColor = VERMELHO,
            focusedLabelColor = VERMELHO,
            unfocusedLabelColor = VERMELHO
        ),
        maxLines = 1
    )
}

@Preview
@Composable
private fun OutlinedTextFieldCustomPreview(){

    var name = ""

    OutlinedTextFieldCustom(
        value = name,
        onValueChange = { name = it },
        label = "Título",
        modifier = Modifier.fillMaxWidth().padding(20.dp)
    )
}