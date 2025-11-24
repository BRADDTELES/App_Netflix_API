package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.UiState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.view.componentes.OutlinedTextFieldCustom
import com.danilloteles.appnetflixapi.viewmodel.FormularioViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FormularioViewModel = viewModel(
        factory = FormularioViewModel.Factory(UserPreferencesRepository(context))
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var listName by remember { mutableStateOf("") }
    var listDescription by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Lista criada com sucesso!")
                delay(500) // Dá tempo para o usuário ver o snackbar
                onNavigateBack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Lista", color = WHITE) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = WHITE
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BLACK,
                    titleContentColor = WHITE,
                    navigationIconContentColor = WHITE
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BLACK)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextFieldCustom(
                value = listName,
                onValueChange = { listName = it },
                label = "Nome da Lista",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 32.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
            OutlinedTextField(
                value = listDescription,
                onValueChange = { listDescription = it },
                label = { Text("Descrição") },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .padding(horizontal = 20.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
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
                maxLines = 30,
                singleLine = false,
            )


            Spacer(modifier = Modifier.padding(bottom = 40.dp))

            if (uiState is UiState.Loading) {
                CircularProgressIndicator(color = VERMELHO)
            } else {
                Button(
                    onClick = {
                        if (listName.isNotBlank()) {
                            viewModel.createList(listName, listDescription)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VERMELHO
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(
                            color = VERMELHO,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SaveAs,
                        contentDescription = "Salvar",
                        tint = WHITE
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SALVAR",
                        color = WHITE,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.padding(bottom = 20.dp))
        }
    }
}

@Preview
@Composable
private fun FormularioScreenPreview() {
    FormularioScreen(onNavigateBack = {})
}