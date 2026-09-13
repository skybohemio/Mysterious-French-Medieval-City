package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Site
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel

@Composable
fun MysteryPlayerBottomBar(
    site: Site?,
    viewModel: GuideViewModel,
    onOpenArticle: (Site) -> Unit,
    langCode: String,
    modifier: Modifier = Modifier
) {
    if (site == null) return

    val isPlaying by viewModel.isTtsPlaying.collectAsState()
    val activeTtsSite by viewModel.activeTtsSite.collectAsState()
    val progress by viewModel.ttsProgress.collectAsState()
    val isPlayingThis = isPlaying && activeTtsSite?.id == site.id

    val title = site.getLocalizedTitle(langCode)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("mystery_player_bottom_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Audio progress line
            if (isPlayingThis) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SandstoneGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail image with click to open article
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenArticle(site) }
                        .testTag("player_thumbnail")
                ) {
                    if (site.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = site.imageUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(RegalBlue, SandstoneGold))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and category
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenArticle(site) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔮 ${site.category}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isPlayingThis) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• 🎧 À vos oreilles",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SandstoneGold
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Controls: Prev, Play/Pause, Next
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.previousSite() },
                        modifier = Modifier.size(36.dp).testTag("btn_player_prev")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Précédent",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            if (isPlayingThis) {
                                viewModel.pauseAudioGuide()
                            } else {
                                viewModel.startAudioGuide(site)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isPlayingThis) SandstoneGold else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("btn_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingThis) "Pause" else "Lecture",
                            tint = if (isPlayingThis) Color.Black else Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextSite() },
                        modifier = Modifier.size(36.dp).testTag("btn_player_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Suivant",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action button: Dévoiler le mystère (Article)
            Button(
                onClick = { onOpenArticle(site) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("btn_open_mystery_article")
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (langCode == "FR") "Dévoiler le secret du lieu 📜" else "Reveal secret article 📜",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
