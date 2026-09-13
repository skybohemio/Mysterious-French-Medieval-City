package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OfflineMapData
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.GuideViewModel

/**
 * Global Options & Settings Screen:
 * Allows the user to customize:
 * - Sound volume, speech speed, voice timbre, GPS proximity autoplay
 * - Audio guide language
 * - Offline mode & OpenStreetMap tile caching
 * - Typography scaling and map styles
 * - CSV export / import and administrator controls
 */
@Composable
fun OptionsScreen(
    viewModel: GuideViewModel,
    onOpenExportCsv: () -> Unit,
    onOpenImportCsv: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.selectedLanguage.collectAsState()
    val isFemaleVoice by viewModel.isFemaleVoice.collectAsState()
    val cachedTiles by viewModel.cachedMapTiles.collectAsState()
    val isOfflineMapEnabled by viewModel.isOfflineMapEnabled.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val audioVolume by viewModel.audioVolume.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val autoPlayGps by viewModel.autoPlayGps.collectAsState()
    val gpsDetectionRadius by viewModel.gpsDetectionRadius.collectAsState()
    val fontSizeScale by viewModel.fontSizeScale.collectAsState()
    val mapStyleIndex by viewModel.mapStyleIndex.collectAsState()

    var testSoundPlayed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
            .testTag("options_screen")
    ) {
        // Options Header
        Surface(
            color = RegalBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Options & Personnalisation",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ajustez vos préférences audio, la langue et le hors-ligne",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    // Account quick button
                    IconButton(
                        onClick = onOpenAuthDialog,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .testTag("btn_options_account")
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Default.PersonOutline,
                            contentDescription = "Mon Compte",
                            tint = if (currentUser != null) SandstoneGold else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: 🎧 AUDIO & ÉCOUTE
        OptionsSectionCard(
            title = "🎧 Niveau Sonore & Audioguide",
            subtitle = "Contrôlez le volume et le timbre de la narration"
        ) {
            // Volume Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (audioVolume == 0f) Icons.Default.VolumeMute else if (audioVolume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = SandstoneGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Volume de la voix",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Text(
                        text = "${(audioVolume * 100).toInt()} %",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = SandstoneGold
                    )
                }

                Slider(
                    value = audioVolume,
                    onValueChange = { viewModel.setAudioVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = SandstoneGold,
                        activeTrackColor = SandstoneGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_audio_volume")
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

            // Speech speed selector
            Text(
                text = "Vitesse de diction",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val speeds = listOf(0.75f to "0.75x", 1.0f to "1.0x (Normal)", 1.25f to "1.25x", 1.5f to "1.5x")
                speeds.forEach { (speedVal, label) ->
                    val isSelected = kotlin.math.abs(speechRate - speedVal) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSpeechRate(speedVal) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SandstoneGold.copy(alpha = 0.25f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("chip_speed_$label")
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

            // Voice Timbre (Female / Male)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voix de la narration",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (isFemaleVoice) "Voix Féminine (Chaleureuse & Claire)" else "Voix Masculine (Grave & Mystérieuse)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isFemaleVoice,
                        onClick = { viewModel.setVoiceGender(true) },
                        label = { Text("Femme") },
                        modifier = Modifier.testTag("chip_voice_female")
                    )
                    FilterChip(
                        selected = !isFemaleVoice,
                        onClick = { viewModel.setVoiceGender(false) },
                        label = { Text("Homme") },
                        modifier = Modifier.testTag("chip_voice_male")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Test Voice Button
            OutlinedButton(
                onClick = {
                    testSoundPlayed = true
                    viewModel.ttsPlayer.speak(
                        "Bienvenue à Bourges, cité des alchimistes et du Berry mystérieux."
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_test_audio_voice")
            ) {
                Icon(Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tester la voix audio 🔊")
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

            // Autoplay GPS Proximity Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Déclenchement GPS automatique",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Lance la narration quand vous arrivez devant un monument",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = autoPlayGps,
                    onCheckedChange = { viewModel.setAutoPlayGps(it) },
                    modifier = Modifier.testTag("switch_autoplay_gps")
                )
            }

            if (autoPlayGps) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rayon de détection sonore :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25 to "25 m (Ruelles)", 50 to "50 m (Places)", 100 to "100 m (Parcs)").forEach { (rMeters, label) ->
                        FilterChip(
                            selected = gpsDetectionRadius == rMeters,
                            onClick = { viewModel.setGpsDetectionRadius(rMeters) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("chip_radius_$rMeters")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 2: 🌍 LANGUE DU GUIDE
        OptionsSectionCard(
            title = "🌍 Langue de l'Audioguide",
            subtitle = "Tous les récits, noms et voix s'adaptent instantanément"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = lang == currentLanguage
                    Surface(
                        color = if (isSelected) SandstoneGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setLanguage(lang) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SandstoneGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("lang_select_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Code : ${lang.code}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sélectionné",
                                    tint = SandstoneGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 3: 💾 MODE HORS-LIGNE & STOCKAGE (OpenStreetMap & Données)
        OptionsSectionCard(
            title = "💾 Mode Hors-Ligne & Données",
            subtitle = "Visitez la vieille ville et les souterrains sans réseau mobile"
        ) {
            // Status banner
            Surface(
                color = if (cachedTiles.isNotEmpty()) Color(0xFF2E7D32).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (cachedTiles.isNotEmpty()) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (cachedTiles.isNotEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (cachedTiles.isNotEmpty()) "Pack Hors-Ligne Prêt (${cachedTiles.size} zones)" else "Mode hors-ligne non synchronisé",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (cachedTiles.isNotEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "170 monuments et récits déjà embarqués dans l'application.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switch toggle offline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Forcer le mode hors-ligne",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Économise votre batterie et votre forfait 4G/5G",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Switch(
                    checked = isOfflineMapEnabled,
                    onCheckedChange = { viewModel.toggleOfflineMap(it) },
                    modifier = Modifier.testTag("switch_force_offline")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Cache Map Tiles
            Button(
                onClick = { viewModel.cacheBourgesOfflineMap() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SandstoneGold,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_cache_map_tiles")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mettre en cache la carte de Bourges", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Clear Cache
            OutlinedButton(
                onClick = { viewModel.clearOfflineTiles() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_clear_offline_cache")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vider le cache de la carte")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 4: 🎨 AFFICHAGE & ERGONOMIE
        OptionsSectionCard(
            title = "🎨 Affichage & Ergonomie",
            subtitle = "Adaptez le style visuel et le confort de lecture"
        ) {
            Text(
                text = "Taille de police des récits historiques",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.0f to "Normale", 1.2f to "Confortable", 1.4f to "Grande (Soleil)").forEach { (scaleVal, label) ->
                    val isSelected = kotlin.math.abs(fontSizeScale - scaleVal) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFontSizeScale(scaleVal) },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("chip_font_$label")
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

            Text(
                text = "Style cartographique",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0 to "OSM Standard", 1 to "Satellite", 2 to "Contraste").forEach { (idx, label) ->
                    FilterChip(
                        selected = mapStyleIndex == idx,
                        onClick = { viewModel.setMapStyle(idx) },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("chip_map_style_$idx")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 5: 📂 GESTION DES DONNÉES & CRÉATEUR (CSV Export / Import / Admin)
        OptionsSectionCard(
            title = "📂 Gestion des Données & Créateur",
            subtitle = "Sauvegardez ou enrichissez la base des 170 points d'intérêt"
        ) {
            Text(
                text = "Ces outils vous permettent de travailler sur le fichier CSV de tous les POIs de Bourges et de réinjecter vos textes.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenExportCsv,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_options_export_csv")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exporter CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onOpenImportCsv,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("btn_options_import_csv")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importer CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenAdminPanel,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_options_admin_panel")
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Espace Administrateur (Gestion & Ventes)")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 6: 🏛️ À PROPOS & PATRIMOINE
        OptionsSectionCard(
            title = "🏛️ À Propos du Guide",
            subtitle = "Les Mystères de Bourges • Édition 2026"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Les Mystères de Bourges",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Application audioguide interactive, patrimoniale et alchimique.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• 170 points d'intérêt répertoriés",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• 8 parcours scénarisés (2 offerts, 6 payants à 9,00 €)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Moteur audio TTS multi-langues avec suivi textuel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Version 2.5.0 • Produit en partenariat avec Radio Bourges",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun OptionsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
