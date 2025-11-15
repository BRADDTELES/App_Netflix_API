package com.danilloteles.appnetflixapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.danilloteles.appnetflixapi.ui.theme.AppNetflixAPITheme
import com.danilloteles.appnetflixapi.view.MenuSection
import com.danilloteles.appnetflixapi.view.NetflixTopBar
import com.danilloteles.appnetflixapi.view.PopularMoviesSection

class MainActivity : ComponentActivity() {
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
fun NetflixScreen() {

    Scaffold(
        topBar = {
            NetflixTopBar()
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

            MenuSection()

            PopularMoviesSection()

        }

    }
}

@Preview
@Composable
fun NetflixScreenPreview() {
    NetflixScreen()
}