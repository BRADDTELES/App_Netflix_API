package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.GRAY_100
import com.danilloteles.appnetflixapi.ui.theme.GRAY_700
import com.danilloteles.appnetflixapi.ui.theme.GRAY_900
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@Composable
fun OutlinedTextFieldCustom(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    modifier: Modifier = Modifier,
    maxLength: Int = 100
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            label = { Text(text = label) },
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                cursorColor = VERMELHO,
                focusedIndicatorColor = VERMELHO,
                unfocusedIndicatorColor = VERMELHO,
                focusedContainerColor = TRANSPARENT,
                unfocusedContainerColor = TRANSPARENT,
                focusedTextColor = VERMELHO,
                unfocusedTextColor = VERMELHO,
                focusedLabelColor = VERMELHO,
                unfocusedLabelColor = VERMELHO
            ),
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        // Feedback de caracteres restantes
        Text(
            text = "${value.length}/$maxLength",
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            color = if (value.length >= maxLength) VERMELHO else GRAY_700
        )
    }
}

@Preview
@Composable
private fun OutlinedTextFieldCustomPreview() {

    var name = ""

    OutlinedTextFieldCustom(
        value = name,
        onValueChange = { name = it },
        label = "Título",
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    )
}