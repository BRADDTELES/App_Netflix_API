/*
*  Componente de Player Moderno
* */
package com.danilloteles.appnetflixapi.view.componentes

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import com.danilloteles.appnetflixapi.R
import com.danilloteles.appnetflixapi.ui.theme.BLACK
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun NetflixVideoPlayer(
    videoKey: String,
    title: String,
    onClose: () -> Unit,
    onOpenInYouTube: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var youTubePlayerRef by remember { mutableStateOf<YouTubePlayer?>(null) }

    DisposableEffect(Unit) {
        // Configurar para tela cheia
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            // Restaurar orientação
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BLACK)
            .clickable { showControls = !showControls }
    ) {
        // YouTube Player View
        AndroidView(
            factory = { context ->
                YouTubePlayerView(context).apply {
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayerRef = youTubePlayer
                            youTubePlayer.loadVideo(videoKey, 0f)
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState
                        ) {
                            isPlaying = when (state) {
                                PlayerConstants.PlayerState.PLAYING -> true
                                PlayerConstants.PlayerState.PAUSED,
                                PlayerConstants.PlayerState.ENDED,
                                PlayerConstants.PlayerState.VIDEO_CUED -> false
                                else -> isPlaying
                            }
                        }
                        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                            hasError = true
                            Log.e("YouTubePlayer", "Erro no player: $error")
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // ⭐ TELA DE ERRO
        if (hasError) {
            ErrorScreen(
                title = title,
                onClose = onClose,
                onOpenInYouTube = onOpenInYouTube,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Controles normais
            NetflixPlayerControls(
                isPlaying = isPlaying,
                title = title,
                onClose = onClose,
                onPlayPause = {
                    youTubePlayerRef?.let { player ->
                        if (isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }
                },
                onOpenInYouTube = onOpenInYouTube,
                visible = showControls,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ErrorScreen(
    title: String,
    onClose: () -> Unit,
    onOpenInYouTube: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(BLACK),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = "Erro",
                tint = VERMELHO,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Vídeo Restrito",
                color = WHITE,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Este trailer não pode ser reproduzido aqui devido a restrições do proprietário.",
                color = WHITE.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WHITE
                    ),
                    border = BorderStroke(1.dp, WHITE)
                ) {
                    Text("Voltar")
                }

                Button(
                    onClick = onOpenInYouTube,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VERMELHO
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.ic_youtube),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Abrir no YouTube")
                }
            }
        }

        // Botão de fechar no canto
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(BLACK.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Fechar",
                tint = WHITE
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NetflixPlayerControls(
    isPlaying: Boolean,
    title: String,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onOpenInYouTube: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(modifier = modifier) {
            // Gradient superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                BLACK.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // TopBar com título e botão fechar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(
                        BLACK.copy(alpha = 0.6f),
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = WHITE,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = title,
                    color = WHITE,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )

                // NOVO: Botão para abrir no YouTube
                IconButton(
                    onClick = onOpenInYouTube,
                    modifier = Modifier.background(BLACK.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_youtube),
                        contentDescription = "Abrir no YouTube",
                        tint = WHITE,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { /* Implementar funcionalidades Tela cheia */ },
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Filled.Fullscreen,
                        contentDescription = "Tela cheia",
                        tint = WHITE,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ⭐ CONTROLE CENTRAL CLICÁVEL
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(
                        if (isPlaying) Color.Transparent else VERMELHO.copy(alpha = 0.9f),
                        CircleShape
                    )
                    .clickable { onPlayPause() }, // ⭐ CLICK PARA PLAY/PAUSE
                contentAlignment = Alignment.Center
            ) {
                if (!isPlaying) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Reproduzir",
                        tint = WHITE,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NetflixVideoPlayerPreview(){
    NetflixVideoPlayer(
        videoKey = "BdJKm16Co6M",
        title = "Fight Club (1999) Trailer - Starring Brad Pitt, Edward Norton, Helena Bonham Carter",
        onClose = {},
        onOpenInYouTube = {},
        modifier = Modifier.fillMaxSize()
    )
}