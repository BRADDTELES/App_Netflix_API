package com.danilloteles.appnetflixapi.view.screens

import android.media.MediaPlayer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
) {

    val context = LocalContext.current

    // Controla o lifecycle do MediaPlayer
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        // Delay para sincronizar com a animação (ex: 1000ms = 1 segundo após a tela aparecer)
        delay(500) // AJUSTE ESSE VALOR CONFORME SUA ANIMAÇÃO
        val player  = MediaPlayer.create(context, R.raw.netflix_intro)
        mediaPlayer = player
        player.start()
        delay(5000) // 6000ms total - 1000ms de delay = 5000ms restantes
        onTimeout()
    }

    // Libera o MediaPlayer quando a composição é descartada
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        var alpha by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(Unit) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 4000,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                alpha = value
            }
        }

        Column(
            modifier = Modifier
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.slogan_transparente),
                contentDescription = "StreamDTC Logo",
                modifier = Modifier.size(200.dp)
            )
            IndeterminateCircularProgressIndicatorSample()
        }

    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(
        onTimeout = {}
    )
}

@Preview
@Composable
fun IndeterminateCircularProgressIndicatorSample() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(
        color = VERMELHO
    ) }
}

@Preview
@Composable
fun IndeterminateLinearProgressIndicatorSample() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { LinearProgressIndicator(
        color = VERMELHO,
        trackColor = BLACK
    ) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun IndeterminateLinearWavyProgressIndicatorSample() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { LinearWavyProgressIndicator(
        color = VERMELHO,
        trackColor = BLACK
    ) }
}