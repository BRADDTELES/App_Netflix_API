package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.TRANSPARENT
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun NetflixVideoPlayer(
    videoKey: String,
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BLACK)
    ) {
        // ExoPlayer integrado aqui
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    // Configurações do player estilo Netflix
                    useController = false // Usar controles customizados
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            }
        )

        // Controles customizados estilo Netflix
        NetflixPlayerControls(
            isPlaying = isPlaying,
            title = title,
            onPlayPause = { isPlaying = !isPlaying },
            onClose = onClose,
            visible = showControls,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetflixPlayerControls(
    isPlaying: Boolean,
    title: String,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = modifier) {
            // TopBar com título e botão fechar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BLACK.copy(alpha = 0.7f),
                                TRANSPARENT
                            )
                        )
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Fechar",
                        tint = WHITE
                    )
                }

                Text(
                    text = title,
                    color = WHITE,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Controle central de play/pause
            FloatingActionButton(
                onClick = onPlayPause,
                modifier = Modifier.align(Alignment.Center),
                containerColor = VERMELHO.copy(alpha = 0.9f)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                    tint = WHITE,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun NetflixVideoPlayerPreview(){
    NetflixVideoPlayer(
        videoKey = "123",
        title = "Título do vídeo",
        onClose = {},
        modifier = Modifier
    )
}