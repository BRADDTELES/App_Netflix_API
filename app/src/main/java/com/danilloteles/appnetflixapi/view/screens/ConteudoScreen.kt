@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
package com.danilloteles.appnetflixapi.view.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilloteles.appnetflixapi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConteudoScreen(
    movieTitle: String,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val initialVideoItems = rememberVideoItems()
    val currentVideoItems = remember { mutableStateListOf(*initialVideoItems.toTypedArray()) }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            delay(2000) // Simula atraso da rede
            currentVideoItems.clear()
            currentVideoItems.addAll(initialVideoItems.shuffled()) // Reordena para simular novos dados
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar( // Alterado para SmallTopAppBar para um tamanho menor
                title = {
                    Text(
                        text = movieTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors( // Usando colors para SmallTopAppBar
                    containerColor = Color.Red,
                    actionIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White // Adicionado cor para o ícone de navegação
                ),
                navigationIcon = { // Botão de retorno
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, "Trigger Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            ConnectedButtonGroupComposable()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentVideoItems) { item ->
                        VideoItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItem(item: VideoData) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable{  },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier
                .width(160.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.FillHeight,
        )
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ConnectedButtonGroupComposable() {
    val options = listOf("Favoritos", "Curtido", "A-Z")
    val unCheckedIcons =
        listOf(Icons.Filled.FavoriteBorder, Icons.Outlined.ThumbUp, Icons.Default.Abc)
    val checkedIcons = listOf(Icons.Filled.Favorite, Icons.Filled.ThumbUp, Icons.Filled.Abc)
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val modifiers = listOf(
            Modifier.weight(1.1f),
            Modifier.weight(1.1f),
            Modifier.weight(1f),
        )

        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { selectedIndex = index },
                modifier = modifiers[index].semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    checkedContainerColor = Color.Red,
                    checkedContentColor = Color.White,
                    containerColor = Color.White,
                    contentColor = Color.Gray
                )
            ) {
                Icon(
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

data class VideoData(
    val title: String,
    val imageRes: Int
)

@Composable
fun rememberVideoItems(): List<VideoData> {
    return listOf(
        VideoData("Item 1", R.drawable.movie_show_vizinha),
        VideoData("Item 2", R.drawable.movie_star_war),
        VideoData("Item 3", R.drawable.movie_senhor_dos_aneis),
        VideoData("Item 4", R.drawable.movie_interstellar),
        VideoData("Item 5", R.drawable.movie_matrix),
        VideoData("Item 6", R.drawable.movie_fight_club),
        VideoData("Item 7", R.drawable.movie_dark_knight),
        VideoData("Item 8", R.drawable.movie_liga_da_justica),
        VideoData("Item 9", R.drawable.movie_pulp_fiction),
        VideoData("Item 10", R.drawable.movie_todo_mundo_panico),
    )
}

@Preview
@Composable
private fun ConteudoScreenPreview(){
    ConteudoScreen(
        movieTitle = "Lista de videos",
        onBackClick = {}
    )
}