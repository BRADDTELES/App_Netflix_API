package com.danilloteles.appnetflixapi

import android.widget.Space
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
) {

    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
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
                    durationMillis = 1000,
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
                painter = painterResource(R.drawable.ic_netflix_splash),
                contentDescription = "Netflix Logo",
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(VERMELHO)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                color = VERMELHO,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
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