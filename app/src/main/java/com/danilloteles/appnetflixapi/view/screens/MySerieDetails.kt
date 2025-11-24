package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AddToQueue
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.danilloteles.appnetflixapi.constantes.Constantes
import com.danilloteles.appnetflixapi.datasource.datastore.UserPreferencesRepository
import com.danilloteles.appnetflixapi.model.serie.SerieDetalhes
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.GRAY_100
import com.danilloteles.appnetflixapi.ui.theme.GRAY_900
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.danilloteles.appnetflixapi.utils.events.UiState
import com.danilloteles.appnetflixapi.view.componentes.LoadingIndicatorCustom
import com.danilloteles.appnetflixapi.viewmodel.MySerieDetailsViewModel

@Composable
fun MySerieDetails(
    serieId: Int,
    listId: String?,
    onClick: (Int) -> Unit
) {
    val viewModel: MySerieDetailsViewModel = viewModel(
        factory = MySerieDetailsViewModel.Factory(
            serieId,
            UserPreferencesRepository(LocalContext.current),
            listId
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Idle -> {}
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BLACK),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicatorCustom(animationDelay = 5000)
            }
        }

        is UiState.Success -> {
            ConteudoMySerieDetails(
                serie = state.data,
                viewModel = viewModel,
                onClick = onClick,
                listId = listId
            )
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BLACK),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.message, color = WHITE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConteudoMySerieDetails(
    serie: SerieDetalhes,
    viewModel: MySerieDetailsViewModel?,
    onClick: (Int) -> Unit,
    listId: String?
) {
    val isInMyList by viewModel?.isInMyList?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val myListActionUiState by viewModel?.myListActionUiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(UiState.Idle) }
    val userListsUiState by viewModel?.userListsUiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(UiState.Idle) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showListSelection by remember { mutableStateOf(false) }

    LaunchedEffect(myListActionUiState) {
        when (val state = myListActionUiState) {
            is UiState.Success -> {
                val message = if (isInMyList) "Série adicionado à lista!" else "Série removido da lista."
                snackbarHostState.showSnackbar(message)
                viewModel?.resetMyListActionUiState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel?.resetMyListActionUiState()
            }
            else -> {}
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        BottomSheetScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).padding(it),
            sheetContent = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = Constantes.IMAGE_BASE_URL + serie.poster_path,
                        contentDescription = "Capa da série",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                        contentScale = ContentScale.Crop
                    )

                }
            },
            scaffoldState = scaffoldState,
            sheetPeekHeight = 400.dp,
            sheetMaxWidth = 620.dp,
            sheetContainerColor = GRAY_900,
            sheetDragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier
                            .size(width = 32.dp, height = 4.dp)
                            .background(GRAY_100, MaterialTheme.shapes.extraLarge)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Netflix",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Red
                    ),
                    scrollBehavior = scrollBehavior,
                )
            },
            containerColor = BLACK,
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(BLACK)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box {

                            var splitButtonChecked by remember { mutableStateOf(false) }

                            SplitButtonLayout(
                                leadingButton = {
                                    SplitButtonDefaults.TonalLeadingButton(
                                        onClick = {},
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = WHITE,
                                            contentColor = GRAY_900
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
                                            contentDescription = "Ações da série",
                                        )
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text("Opções")
                                    }
                                },
                                trailingButton = {
                                    val description = "Toggle Button"
                                    TooltipBox(
                                        positionProvider =
                                            TooltipDefaults.rememberTooltipPositionProvider(
                                                TooltipAnchorPosition.Above),
                                        tooltip = { PlainTooltip { Text(description) } },
                                        state = rememberTooltipState(),
                                    ) {
                                        SplitButtonDefaults.TonalTrailingButton(
                                            checked = splitButtonChecked,
                                            onCheckedChange = { splitButtonChecked = it },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = WHITE,
                                                contentColor = GRAY_900
                                            ),
                                            modifier =
                                                Modifier.semantics {
                                                    stateDescription = if (splitButtonChecked) "Expanded" else "Collapsed"
                                                    contentDescription = description
                                                },
                                        ) {
                                            val rotation: Float by
                                            animateFloatAsState(
                                                targetValue = if (splitButtonChecked) 180f else 0f,
                                                label = "Rotacionar ícone",
                                            )
                                            Icon(
                                                Icons.Filled.KeyboardArrowDown,
                                                modifier =
                                                    Modifier.size(SplitButtonDefaults.TrailingIconSize).graphicsLayer {
                                                        this.rotationZ = rotation
                                                    },
                                                contentDescription = "Expandir menu",
                                            )
                                        }
                                    }
                                },
                            )
                            DropdownMenu(
                                expanded = splitButtonChecked,
                                onDismissRequest = {
                                    splitButtonChecked = false
                                    showListSelection = false // Garante que a seleção também feche
                                }
                            ) {
                                if (isInMyList) {
                                    DropdownMenuItem(
                                        text = { Text("Remover da Lista") },
                                        onClick = {
                                            viewModel?.addOrRemoveSerie(listId)
                                            splitButtonChecked = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Delete, contentDescription = null)
                                        },
                                    )
                                } else if (showListSelection) {
                                    // Exibe um título para o submenu
                                    DropdownMenuItem(
                                        text = { Text("Selecione uma lista", fontWeight = FontWeight.Bold) },
                                        onClick = {},
                                        enabled = false // Desativa o clique
                                    )

                                    when (val state = userListsUiState) {
                                        is UiState.Success -> {
                                            if (state.data.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("Nenhuma lista encontrada") },
                                                    onClick = { },
                                                    enabled = false
                                                )
                                            } else {
                                                state.data.forEach { list ->
                                                    DropdownMenuItem(
                                                        text = { Text(list.name) },
                                                        onClick = {
                                                            viewModel?.addOrRemoveSerie(list.id.toString())
                                                            // Fecha tudo após a seleção
                                                            showListSelection = false
                                                            splitButtonChecked = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        is UiState.Loading -> {
                                            DropdownMenuItem(text = { Text("Carregando listas...") }, onClick = {}, enabled = false)
                                        }
                                        is UiState.Error -> {
                                            DropdownMenuItem(text = { Text("Erro ao carregar") }, onClick = {}, enabled = false)
                                        }
                                        else -> {} // UiState.Idle
                                    }
                                } else {
                                    // Botão inicial para "Adicionar à Lista"
                                    DropdownMenuItem(
                                        text = { Text("Adicionar à Lista") },
                                        onClick = { showListSelection = true }, // Ativa o modo de seleção de lista
                                        leadingIcon = {
                                            Icon(Icons.Outlined.AddToQueue, contentDescription = null)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
                item {
                    Text(
                        text = serie.name,
                        color = WHITE,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Text(
                        text = serie.overview,
                        color = WHITE,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MySerieDetailsPreview(){
    MySerieDetails(
        serieId = 1,
        listId = null,
        onClick = {}
    )
}