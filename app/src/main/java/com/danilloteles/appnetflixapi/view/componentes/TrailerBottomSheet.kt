/*
* Bottom Sheet para Trailers (Material 3 Expressive)
* */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.danilloteles.appnetflixapi.view.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilloteles.appnetflixapi.model.video.Video
import com.danilloteles.appnetflixapi.ui.theme.GRAY_300
import com.danilloteles.appnetflixapi.ui.theme.GRAY_700
import com.danilloteles.appnetflixapi.ui.theme.GRAY_900
import com.danilloteles.appnetflixapi.ui.theme.VERMELHO
import com.danilloteles.appnetflixapi.ui.theme.WHITE

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrailerBottomSheet(
    videos: List<Video>,
    onVideoSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = GRAY_900,
        contentColor = WHITE,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = GRAY_300
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Trailers e Vídeos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = WHITE
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(videos) { video ->
                TrailerItem(
                    video = video,
                    onClick = { onVideoSelected(video.key) }
                )
            }
        }
    }
}

@Composable
fun TrailerItem(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = GRAY_700
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.PlayCircleFilled,
                contentDescription = null,
                tint = VERMELHO,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = WHITE,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${video.type} • ${video.site}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GRAY_300
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = GRAY_300
            )
        }
    }
}

@Preview
@Composable
private fun TrailerBottomSheetPreview(){
    TrailerBottomSheet(
        videos = listOf(
            Video(
                iso_639_1 = "en",
                iso_3166_1 = "US",
                name = "Fight Club (1999) Trailer - Starring Brad Pitt, Edward Norton, Helena Bonham Carter",
                key = "O-b2VfmmbyA",
                site = "YouTube",
                size = 720,
                type = "Trailer",
                official = false,
                published_at = "",
                videoId = "639d5326be6d88007f170f44"
            )
        ),
        onVideoSelected = {},
        onDismiss = {}
    )
}