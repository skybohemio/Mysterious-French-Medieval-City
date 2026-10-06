package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.GeminiImageState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Site
import com.example.ui.theme.SandstoneGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    site: Site,
    onBack: () -> Unit,
    onPlayClick: (Site) -> Unit,
    onPauseClick: () -> Unit,
    activeTtsSite: Site?,
    isTtsPlaying: Boolean,
    langCode: String,
    geminiImageState: GeminiImageState,
    onShowOnMap: () -> Unit,
    onPlayArticleClick: ((Site) -> Unit)? = null,
    onLanguageChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isSpeakingActive = activeTtsSite?.id == site.id && isTtsPlaying

    // Map site category to icon and color
    val catColor = remember(site.category) {
        when (site.category) {
            "CATHEDRAL" -> Color(0xFFC5A059)
            "PALACE" -> Color(0xFF4A154B)
            "NATURE" -> Color(0xFF1E4D2B)
            "MUSEUM" -> Color(0xFFE65100)
            else -> Color(0xFF6750A4)
        }
    }

    val catIcon = remember(site.category) {
        when (site.category) {
            "CATHEDRAL" -> Icons.Default.Church
            "PALACE" -> Icons.Default.Castle
            "NATURE" -> Icons.Default.NaturePeople
            "MUSEUM" -> Icons.Default.Museum
            else -> Icons.Default.LocationOn
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val imageRes = remember(site) { getSiteImageRes(site) }
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = site.getLocalizedTitle(langCode),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Back Button Overlay
                FilledIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .align(Alignment.TopStart)
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Locales.string("back_to_list", langCode)
                    )
                }

                // Dark Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 400f
                            )
                        )
                )

                // Title overlay at the bottom of image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = site.getLocalizedTitle(langCode),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Badge & Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge
                    Row(
                        modifier = Modifier
                            .background(catColor.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                            .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getCategoryLabel(site.category, langCode),
                            style = MaterialTheme.typography.labelLarge,
                            color = catColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // View on Map Button
                    FilledTonalButton(
                        onClick = onShowOnMap,
                        modifier = Modifier.testTag("detail_show_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = Locales.string("map_view", langCode),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Locales.string("map_view", langCode),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Description Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (langCode == "FR") "À propos de ce monument" else "About this monument",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Text(
                            text = site.getLocalizedDescription(langCode),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Mystery Curiosity Article Card
                val articleText = if (site.mysteryArticle.isNotBlank()) site.mysteryArticle else site.getLocalizedDescription(langCode)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SandstoneGold.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SandstoneGold.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("site_detail_mystery_article_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📜", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (langCode == "FR") "Article & Curiosité du Lieu" else "Curiosity & Mystery Article",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        HorizontalDivider(color = SandstoneGold.copy(alpha = 0.25f))

                        Text(
                            text = articleText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FilledTonalButton(
                            onClick = {
                                if (isSpeakingActive) {
                                    onPauseClick()
                                } else {
                                    onPlayArticleClick?.invoke(site) ?: onPlayClick(site)
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSpeakingActive) SandstoneGold else MaterialTheme.colorScheme.primary,
                                contentColor = if (isSpeakingActive) Color.Black else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeakingActive) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpeakingActive) {
                                    if (langCode == "FR") "Arrêter la récitation" else "Stop recitation"
                                } else {
                                    if (langCode == "FR") "Écouter la récitation de l'article" else "Listen to article recitation"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Gemini AI Insights Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (langCode == "FR") "✨ Secrets de Bourges par Gemini" else "✨ Bourges Secrets by Gemini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        
                        when (geminiImageState) {
                            is GeminiImageState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 3.dp
                                        )
                                        Text(
                                            text = if (langCode == "FR") "Génération par l'IA..." else "Generating with AI...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            is GeminiImageState.Success -> {
                                // Beautiful dynamic photo from Gemini loaded via Coil
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(geminiImageState.imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Dynamic Landmark Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                                    startY = 100f
                                                )
                                            )
                                    )
                                    
                                    Text(
                                        text = if (langCode == "FR") "Image Haute Résolution par l'IA" else "AI High-Resolution Photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Secret Trivia Section
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (langCode == "FR") "Le Secret Historique :" else "The Historical Secret:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = geminiImageState.secretTrivia,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // Visit Tip Section
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (langCode == "FR") "Conseil de visite Gemini :" else "Gemini Visit Tip:",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = geminiImageState.visitTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = if (langCode == "FR") "Détails non disponibles actuellement." else "AI Details currently unavailable.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Audio Guide Interactive Control Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Headset,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = Locales.string("immersive_audio_guide", langCode),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2E7D32)
                                        ) {
                                            Text(
                                                text = "⚡ HORS-LIGNE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${site.audioDurationSec} sec • Toutes les voix & audio embarqués (0 Mo)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Audio Narration Language Selector
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Locales.string("narration_language", langCode),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = AppLanguage.values().firstOrNull { it.code.equals(langCode, ignoreCase = true) }?.let { "${it.flag} ${it.displayName}" } ?: langCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = lang.code.equals(langCode, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onLanguageChange?.invoke(lang.code)
                                            }
                                            .testTag("detail_audio_lang_${lang.code}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = lang.flag, fontSize = 14.sp)
                                            Text(
                                                text = lang.code,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Play/Pause Action Button
                        Button(
                            onClick = {
                                if (isSpeakingActive) onPauseClick() else onPlayClick(site)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_play_audio_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeakingActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpeakingActive) Locales.string("stop_voice", langCode) else Locales.string("start_audio", langCode),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Location Details & Navigation Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (langCode == "FR") "Coordonnées & Parcours" else "Coordinates & Directions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Latitude : ${String.format("%.5f", site.latitude)}° N",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Longitude : ${String.format("%.5f", site.longitude)}° E",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    val gmmIntentUri = android.net.Uri.parse("geo:${site.latitude},${site.longitude}?q=${android.net.Uri.encode(site.getLocalizedTitle(langCode))}")
                                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                    context.startActivity(mapIntent)
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.testTag("detail_gps_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Navigation,
                                    contentDescription = Locales.string("gps_navigation", langCode),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Locales.string("gps_navigation", langCode),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

// Helper to resolve drawable image resource for each site
fun getSiteImageRes(site: Site): Int {
    return when (site.id) {
        1 -> R.drawable.img_cathedrale
        2 -> R.drawable.img_palais_jacques_coeur
        3 -> R.drawable.img_marais
        4 -> R.drawable.img_jardin_archeveche
        5 -> R.drawable.img_musee_berry
        else -> {
            // Fallback to category base
            when (site.category) {
                "CATHEDRAL" -> R.drawable.img_cathedrale
                "PALACE" -> R.drawable.img_palais_jacques_coeur
                "NATURE" -> R.drawable.img_marais
                "MUSEUM" -> R.drawable.img_musee_berry
                else -> R.drawable.img_cathedrale
            }
        }
    }
}

private fun getCategoryLabel(category: String, lang: String): String {
    return when (category.uppercase()) {
        "TODOS" -> when (lang) {
            "FR" -> "TOUT"
            "EN" -> "ALL"
            "DE" -> "ALLE"
            "NL" -> "ALLES"
            else -> "TODOS"
        }
        "CATHEDRAL" -> when (lang) {
            "FR" -> "CATHÉDRALE"
            "EN" -> "CATHEDRAL"
            "DE" -> "KATHEDRALE"
            "NL" -> "KATHEDRAAL"
            else -> "CATEDRAL"
        }
        "PALACE" -> when (lang) {
            "FR" -> "PALAIS"
            "EN" -> "PALACE"
            "DE" -> "PALAST"
            "NL" -> "PALEIS"
            else -> "PALACIO"
        }
        "NATURE" -> when (lang) {
            "FR" -> "NATURE"
            "EN" -> "NATURE"
            "DE" -> "NATUR"
            "NL" -> "NATUUR"
            else -> "NATURALEZA"
        }
        "MUSEUM" -> when (lang) {
            "FR" -> "MUSÉE"
            "EN" -> "MUSEUM"
            "DE" -> "MUSEUM"
            "NL" -> "MUSEUM"
            else -> "MUSEO"
        }
        "CUSTOM" -> when (lang) {
            "FR" -> "PERSO"
            "EN" -> "CUSTOM"
            "DE" -> "EIGENE"
            "NL" -> "EIGEN"
            else -> "PERSONALIZADOS"
        }
        else -> category
    }
}
