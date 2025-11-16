package com.danilloteles.appnetflixapi.view.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.danilloteles.appnetflixapi.view.navigation.NetflixApp
import com.danilloteles.appnetflixapi.model.Movie
import com.danilloteles.appnetflixapi.retrofit.RetrofitHelper
import com.danilloteles.appnetflixapi.view.componentes.FloatingActionButtonCustom
import com.danilloteles.appnetflixapi.view.componentes.MenuSection
import com.danilloteles.appnetflixapi.view.componentes.NetflixTopBar
import com.danilloteles.appnetflixapi.view.componentes.PopularMoviesSection

class MainActivity : ComponentActivity() {

    private val filmeAPI by lazy {
        RetrofitHelper.filmeAPI
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NetflixApp()
        }
    }
}

@Composable
fun NetflixScreen(
    onMovieClick: (Movie) -> Unit,
    onAddClick: () -> Unit
) {

    Scaffold(
        topBar = {
            NetflixTopBar()
        },
        floatingActionButton = {
            FloatingActionButtonCustom(
                onAddClick = onAddClick
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

            MenuSection()

            PopularMoviesSection(
                onMovieClick = onMovieClick
            )

        }

    }
}

@Preview
@Composable
fun NetflixScreenPreview() {
    NetflixScreen(
        onMovieClick = {},
        onAddClick = {}
    )
}