package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Site
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel

@Composable
fun AudioPlayerController(
    viewModel: GuideViewModel,
    modifier: Modifier = Modifier
) {
    val activeSite by viewModel.activeTtsSite.collectAsStateWithLifecycle()
    val selectedSite by viewModel.selectedSite.collectAsStateWithLifecycle()
    val sites by viewModel.sitesList.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isTtsPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.ttsProgress.collectAsStateWithLifecycle()
    val wordRange by viewModel.ttsWordHighlight.collectAsStateWithLifecycle()
    val speed by viewModel.ttsSpeed.collectAsStateWithLifecycle()
    val isFemaleVoice by viewModel.isFemaleVoice.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var isExpanded by remember { mutableStateOf(false) }

    val currentSite = activeSite ?: selectedSite ?: sites.firstOrNull()

    if (currentSite == null) return

    val isCurrentPlaying = isPlaying && (activeSite?.id == currentSite.id)
    val siteTitle = currentSite.getLocalizedTitle(currentLanguage.code)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("audio_player_root")
    ) {
        if (!isExpanded) {
            // Minimized Bar (Floating Mini-Player)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = true }
                    .testTag("mini_player_bar")
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Soundwave Equalizer indicator
                        SoundWaveVisualizer(isPlaying = isCurrentPlaying)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = siteTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                            Text(
                                text = if (isCurrentPlaying) {
                                    "${com.example.ui.screens.Locales.string("voice_narrator_sub", currentLanguage.code)} • ${currentSite.category}"
                                } else {
                                    "${currentSite.category} • ${com.example.ui.screens.Locales.string("voice_narrator", currentLanguage.code)}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }

                        // Play/Pause Action
                        IconButton(
                            onClick = {
                                if (isCurrentPlaying) {
                                    viewModel.pauseAudioGuide()
                                } else {
                                    viewModel.startAudioGuide(currentSite)
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(42.dp)
                                .testTag("mini_player_toggle")
                        ) {
                            Icon(
                                imageVector = if (isCurrentPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isCurrentPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Expand view button
                        IconButton(
                            onClick = { isExpanded = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Expand Player",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Progress linear indicator at the bottom edge of card
                    LinearProgressIndicator(
                        progress = if (activeSite?.id == currentSite.id) progress else 0f,
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    )
                }
            }
        } else {
            // Full Screen Dialog/Modal Card Overlay
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .testTag("expanded_player_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = com.example.ui.screens.Locales.string("voice_narrator", currentLanguage.code),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize Player",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Site Info & Category Tag
                    Text(
                        text = siteTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val badgeBg = when (currentSite.category) {
                            "CATHEDRAL" -> Color(0xFFF9F3E5)
                            "PALACE" -> Color(0xFFF3E5F5)
                            "NATURE" -> Color(0xFFE8F5E9)
                            "MUSEUM" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFE3F2FD)
                        }
                        val badgeColor = when (currentSite.category) {
                            "CATHEDRAL" -> Color(0xFFC5A059)
                            "PALACE" -> Color(0xFF4A154B)
                            "NATURE" -> Color(0xFF1E4D2B)
                            "MUSEUM" -> Color(0xFFE65100)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentSite.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (currentSite.isPreset) com.example.ui.screens.Locales.string("official_audio", currentLanguage.code) else com.example.ui.screens.Locales.string("local_audio", currentLanguage.code),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✨ ${com.example.ui.screens.Locales.string("edge_tts_badge", currentLanguage.code)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Highlight Text Scroller
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val scrollState = rememberScrollState()

                        // Auto-scroll logic as reading progresses
                        val narrationText = currentSite.getLocalizedNarration(currentLanguage.code)
                        LaunchedEffect(wordRange) {
                            if (wordRange.first > 0) {
                                val textLen = narrationText.length
                                if (textLen > 0) {
                                    val scrollFraction = wordRange.first.toFloat() / textLen.toFloat()
                                    val targetScroll = (scrollState.maxValue * scrollFraction).toInt()
                                    scrollState.animateScrollTo(targetScroll, tween(300))
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(scrollState)
                        ) {
                            val annotatedString = buildAnnotatedString {
                                val fullText = narrationText
                                val activeStart = wordRange.first.coerceIn(0, fullText.length)
                                val activeEnd = wordRange.second.coerceIn(0, fullText.length)

                                if (activeStart > 0) {
                                    append(fullText.substring(0, activeStart))
                                }
                                if (activeStart < activeEnd) {
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            background = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        append(fullText.substring(activeStart, activeEnd))
                                    }
                                    if (activeEnd < fullText.length) {
                                        append(fullText.substring(activeEnd, fullText.length))
                                    }
                                } else {
                                    append(fullText)
                                }
                            }

                            Text(
                                text = annotatedString,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 26.sp,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Media progress bar & timings
                    Column {
                        Slider(
                            value = progress,
                            onValueChange = { /* TTS doesn't easily support arbitrary seeking natively, so we keep it read-only or show simulation feedback */ },
                            modifier = Modifier.fillMaxWidth().testTag("media_scrubber")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val elapsedSec = (currentSite.audioDurationSec * progress).toInt()
                            Text(
                                text = String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = String.format("%02d:%02d", currentSite.audioDurationSec / 60, currentSite.audioDurationSec % 60),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Control Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voice Gender (Female / Male Edge Voice) Selector
                        TextButton(
                            onClick = {
                                viewModel.setTtsVoiceGender(!isFemaleVoice)
                                if (isCurrentPlaying) {
                                    viewModel.startAudioGuide(currentSite)
                                }
                            },
                            modifier = Modifier.testTag("voice_gender_selector")
                        ) {
                            Icon(
                                imageVector = if (isFemaleVoice) Icons.Default.Female else Icons.Default.Male,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFemaleVoice) {
                                    com.example.ui.screens.Locales.string("voice_female", currentLanguage.code)
                                } else {
                                    com.example.ui.screens.Locales.string("voice_male", currentLanguage.code)
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        // Speed rate selector
                        Box {
                            var showSpeedMenu by remember { mutableStateOf(false) }
                            TextButton(
                                onClick = { showSpeedMenu = true },
                                modifier = Modifier.testTag("speed_selector")
                            ) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${speed}x", style = MaterialTheme.typography.labelLarge)
                            }
                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false }
                            ) {
                                listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text("${r}x") },
                                        onClick = {
                                            viewModel.setTtsSpeed(r)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Play/Pause central giant button
                        IconButton(
                            onClick = {
                                if (isCurrentPlaying) {
                                    viewModel.pauseAudioGuide()
                                } else {
                                    viewModel.startAudioGuide(currentSite)
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .testTag("expanded_player_toggle")
                        ) {
                            Icon(
                                imageVector = if (isCurrentPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isCurrentPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Stop/Reset button
                        IconButton(
                            onClick = {
                                viewModel.pauseAudioGuide()
                                isExpanded = false
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop narration",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundWaveVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Waves")
    
    val heights = List(6) { index ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 250 + (index * 80),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "WaveHeight_$index"
            )
        } else {
            remember { mutableFloatStateOf(0.15f) }
        }
    }

    Row(
        modifier = Modifier.height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { hVal ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(hVal.value)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }
    }
}
