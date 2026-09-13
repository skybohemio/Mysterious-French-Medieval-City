package com.example.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Site
import com.example.data.TourRoute
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.viewmodel.GuideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourRouteCartAndArticleSheet(
    route: TourRoute,
    sites: List<Site>,
    onDismiss: () -> Unit,
    onStartRoute: (TourRoute) -> Unit,
    onOpenStripeCheckout: (TourRoute) -> Unit,
    viewModel: GuideViewModel,
    langCode: String
) {
    val routeSites = remember(route.siteIds, sites) {
        route.siteIds.mapNotNull { id -> sites.find { it.id == id } }
    }

    val isAvailable = !route.isPaid || route.isPurchased

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("tour_route_cart_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Hero Curiosity Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                if (route.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = route.imageUrl,
                        contentDescription = route.getLocalizedName(langCode),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(RegalBlue, Color(0xFF0D1B2A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = SandstoneGold,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Price Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAvailable) Color(0xFF2E7D32) else SandstoneGold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (!route.isPaid) Icons.Default.CardGiftcard else if (route.isPurchased) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isAvailable) Color.White else RegalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!route.isPaid) "GRATUIT" else if (route.isPurchased) "DÉJÀ ACQUIS" else "9,00 €",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isAvailable) Color.White else RegalBlue
                        )
                    }
                }

                // Title and duration on overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = route.getLocalizedName(langCode),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 25.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SandstoneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${route.durationMin} min",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = SandstoneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${routeSites.size} curiosités",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Article Curiosité du Panier
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SandstoneGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isTtsPlaying by viewModel.isTtsPlaying.collectAsState()
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = SandstoneGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = route.getLocalizedArticleTitle(langCode),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    if (isTtsPlaying) {
                                        viewModel.pauseAudioGuide()
                                    } else {
                                        viewModel.speakText(
                                            route.getLocalizedArticleTitle(langCode) + ". " + route.getLocalizedArticleContent(langCode)
                                        )
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = SandstoneGold,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTtsPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Écouter l'article",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = route.getLocalizedArticleContent(langCode),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Points d'intérêt inclus
                Text(
                    text = "Étapes & Curiosités du parcours :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                routeSites.forEachIndexed { index, site ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step number badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SandstoneGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RegalBlue
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))

                            // Site thumbnail if exists
                            if (site.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = site.imageUrl,
                                    contentDescription = site.getLocalizedTitle(langCode),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = site.getLocalizedTitle(langCode),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = site.category,
                                    fontSize = 11.sp,
                                    color = SandstoneGold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button: Start or Buy with Stripe
                if (isAvailable) {
                    Button(
                        onClick = {
                            onStartRoute(route)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_route_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explorer ce parcours sur la carte",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onOpenStripeCheckout(route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SandstoneGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("buy_route_stripe_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = RegalBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Commander ce parcours - 9,00 € (Stripe 💳)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RegalBlue
                        )
                    }
                }
            }
        }
    }
}
