package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Site
import com.example.data.TourRoute
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold

/**
 * Landing Page: Choix de parcours immersifs de Bourges.
 * Présente chaque parcours avec un court texte explicatif,
 * son statut de tarification (Gratuit vs Payant 9 €) et ses étapes clés.
 */
@Composable
fun TourSelectionScreen(
    tourRoutes: List<TourRoute>,
    sites: List<Site>,
    selectedRoute: TourRoute?,
    onSelectRouteOnMap: (TourRoute) -> Unit,
    onInspectRouteCart: (TourRoute) -> Unit,
    langCode: String,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("Tous les parcours (${tourRoutes.size})", "Offerts / Gratuits (2)", "Circuits Guidés 9 € (6)")

    val filteredRoutes = remember(selectedFilterIndex, tourRoutes) {
        when (selectedFilterIndex) {
            1 -> tourRoutes.filter { !it.isPaid }
            2 -> tourRoutes.filter { it.isPaid }
            else -> tourRoutes
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tour_selection_landing_page"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Landing Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                RegalBlue,
                                Color(0xFF0F233E)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Surface(
                        color = SandstoneGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("🎧", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GUIDE AUDIO IMMERSIF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = SandstoneGold
                            )
                        }
                    }

                    Text(
                        text = "Choisissez votre parcours à Bourges",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Découvrez la cité des alchimistes au fil d'itinéraires thématiques contés pas à pas. 2 circuits gratuits d'initiation et 6 parcours thématiques complets.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBadge(
                            icon = Icons.Default.Place,
                            title = "${sites.size} Lieux",
                            subtitle = "Géolocalisés",
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            icon = Icons.Default.Route,
                            title = "${tourRoutes.size} Parcours",
                            subtitle = "Thématiques",
                            modifier = Modifier.weight(1f)
                        )
                        StatBadge(
                            icon = Icons.Default.CardGiftcard,
                            title = "2 Offerts",
                            subtitle = "Accès libre",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Filter chips bar
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters.size) { idx ->
                    FilterChip(
                        selected = selectedFilterIndex == idx,
                        onClick = { selectedFilterIndex = idx },
                        label = {
                            Text(
                                text = filters[idx],
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedFilterIndex == idx) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SandstoneGold.copy(alpha = 0.25f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("filter_chip_$idx")
                    )
                }
            }
        }

        // List of Tour Route Cards
        items(filteredRoutes, key = { it.id }) { route ->
            val isCurrentActive = selectedRoute?.id == route.id
            val routeSites = remember(route.siteIds, sites) {
                route.siteIds.mapNotNull { sId -> sites.find { it.id == sId } }
            }

            TourRouteCard(
                route = route,
                routeSites = routeSites,
                isActive = isCurrentActive,
                onExploreOnMap = { onSelectRouteOnMap(route) },
                onInspectCart = { onInspectRouteCart(route) },
                langCode = langCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SandstoneGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun TourRouteCard(
    route: TourRoute,
    routeSites: List<Site>,
    isActive: Boolean,
    onExploreOnMap: () -> Unit,
    onInspectCart: () -> Unit,
    langCode: String,
    modifier: Modifier = Modifier
) {
    val isFree = !route.isPaid
    val isPurchased = route.isPurchased

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 6.dp else 2.dp),
        modifier = modifier
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) SandstoneGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("tour_card_${route.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Theme Badge & Free / Paid Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route duration & steps chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⏱️ ${route.durationMin} min",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📍 ${route.siteIds.size} étapes",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Price pill
                if (isFree) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "● GRATUIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32),
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                } else if (isPurchased) {
                    Surface(
                        color = Color(0xFF00897B).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DÉBLOQUÉ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00897B)
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        color = SandstoneGold.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SandstoneGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "9,00 €",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = route.getLocalizedName(langCode),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Short narrative description
            Text(
                text = route.getLocalizedDescription(langCode),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Key sites preview chips
            if (routeSites.isNotEmpty()) {
                Text(
                    text = "Lieux traversés :",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(routeSites.take(4)) { site ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = site.getLocalizedTitle(langCode),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1
                            )
                        }
                    }
                    if (routeSites.size > 4) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+${routeSites.size - 4} autres",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action: Explore on Map
                Button(
                    onClick = onExploreOnMap,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) SandstoneGold else RegalBlue,
                        contentColor = if (isActive) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_explore_route_${route.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "Parcours actif 🗺️" else "Lancer sur la carte 🗺️",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Secondary Action: Details & Article / Buy
                OutlinedButton(
                    onClick = onInspectCart,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("btn_details_route_${route.id}")
                ) {
                    Icon(
                        imageVector = if (!isFree && !isPurchased) Icons.Default.ShoppingCart else Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!isFree && !isPurchased) "Débloquer" else "Secrets",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
