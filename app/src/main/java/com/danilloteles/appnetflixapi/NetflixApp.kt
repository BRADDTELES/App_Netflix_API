package com.danilloteles.appnetflixapi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen

@Composable
fun NetflixApp() {

    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
    } else {
        NetflixScreen()
    }

}