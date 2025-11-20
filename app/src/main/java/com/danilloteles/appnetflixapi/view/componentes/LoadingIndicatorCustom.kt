package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import kotlinx.coroutines.delay

@Composable
fun LoadingIndicatorCustom(
    modifier: Modifier = Modifier,
    color: Color = VERMELHO,
    dotSize: Dp = 12.dp,
    spacing: Dp = 8.dp,
    animationDelay: Int = 200
) {
    val dots = listOf(
        remember { Animatable(0.5f) },
        remember { Animatable(0.5f) },
        remember { Animatable(0.5f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * animationDelay.toLong())
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationDelay * 2),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(animatable.value)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview
@Composable
fun LoadingIndicatorCustomPreview() {
    LoadingIndicatorCustom()
}