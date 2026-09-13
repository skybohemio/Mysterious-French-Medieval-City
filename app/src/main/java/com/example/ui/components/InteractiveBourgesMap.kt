package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Site
import com.example.data.TourRoute
import com.example.ui.theme.RegalBlue
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Representation of a scenic viewpoint spot
data class MapViewPoint(
    val titleFr: String,
    val titleEn: String,
    val latitude: Double,
    val longitude: Double,
    val descriptionFr: String,
    val descriptionEn: String,
    val associatedSiteId: Int
)

// Real world optimized scenic viewpoints corresponding to main locations
val bestViewPoints = listOf(
    MapViewPoint(
        titleFr = "Point de Vue de l'Archevêché 📸",
        titleEn = "Archbishop's Panoramic View",
        latitude = 47.0816,
        longitude = 2.4024,
        descriptionFr = "Le meilleur endroit du Jardin pour photographier de flanc les majestueux arcs-boutants de la cathédrale.",
        descriptionEn = "The best spot in the Garden to photograph the cathedral's majestic flying buttresses from the side.",
        associatedSiteId = 1
    ),
    MapViewPoint(
        titleFr = "Perspective Jacques Cœur 📸",
        titleEn = "Jacques Cœur Court Perspective",
        latitude = 47.0843,
        longitude = 2.3929,
        descriptionFr = "Vue imprenable de biais sur la façade ornée de navires et de coquilles Saint-Jacques.",
        descriptionEn = "Stunning view of the palace's decorated facade, ships, and scallop shells from the side.",
        associatedSiteId = 2
    ),
    MapViewPoint(
        titleFr = "Passerelle de la Voiselle 📸",
        titleEn = "Voiselle Canal Footbridge",
        latitude = 47.0868,
        longitude = 2.4042,
        descriptionFr = "Magnifique panorama sauvage sur les canaux maraîchers des marais, idéal pour observer la faune.",
        descriptionEn = "Beautiful wild panorama over the vegetable canals of the marshes, ideal for observing wildlife.",
        associatedSiteId = 3
    ),
    MapViewPoint(
        titleFr = "Terrasse Haute de l'Archevêché 📸",
        titleEn = "Upper Archbishop's Terrace",
        latitude = 47.0818,
        longitude = 2.4026,
        descriptionFr = "Vue plongeante symétrique à la française sur les parterres de fleurs et le flanc sud.",
        descriptionEn = "Symmetrical French-style view over the flowerbeds and the south flank of the cathedral.",
        associatedSiteId = 4
    ),
    MapViewPoint(
        titleFr = "Portail de l'Hôtel Cujas 📸",
        titleEn = "Hôtel Cujas Gate View",
        latitude = 47.0849,
        longitude = 2.3932,
        descriptionFr = "Meilleure perspective sur la magnifique cour d'honneur bicolore de la Renaissance.",
        descriptionEn = "Best perspective of the gorgeous two-tone Renaissance honor courtyard.",
        associatedSiteId = 5
    )
)

/**
 * Computes waypoints that follow real existing streets in Bourges instead of drawing direct straight lines.
 */
fun getStreetPathPoints(routeId: Int, routeSites: List<Site>): List<Pair<Double, Double>> {
    val points = mutableListOf<Pair<Double, Double>>()
    if (routeSites.isEmpty()) return points

    when (routeId) {
        1 -> { // 1. L'Athanor et le Cœur d'Or : Les Incontournables (sites: 1, 4, 7, 2)
            val s1 = routeSites.find { it.id == 1 } // Cathédrale
            val s4 = routeSites.find { it.id == 4 } // Horloge Astronomique
            val s7 = routeSites.find { it.id == 7 } // Crypte
            val s2 = routeSites.find { it.id == 2 } // Palais Jacques Cœur
            
            if (s1 != null) points.add(s1.latitude to s1.longitude)
            if (s4 != null) points.add(s4.latitude to s4.longitude)
            if (s7 != null) points.add(s7.latitude to s7.longitude)
            points.add(47.0815 to 2.3995) // Rue Moyenne
            points.add(47.0828 to 2.3970) // Rue des Armuriers
            points.add(47.0840 to 2.3935) // Rue Jacques Cœur
            if (s2 != null) points.add(s2.latitude to s2.longitude)
        }
        2 -> { // 2. L'Échappée Verte & Légende de la Vouivre (sites: 13, 5, 19)
            val s13 = routeSites.find { it.id == 13 } // Jardin de l'Archevêché
            val s5 = routeSites.find { it.id == 5 }   // Les Marais
            val s19 = routeSites.find { it.id == 19 } // Église Saint-Bonnet
            
            if (s13 != null) points.add(s13.latitude to s13.longitude)
            points.add(47.0825 to 2.4030) // Rue de l'Évêché
            points.add(47.0848 to 2.4040) // Rue Édouard Branly
            points.add(47.0868 to 2.4042) // Passerelle Voiselle
            if (s5 != null) points.add(s5.latitude to s5.longitude)
            points.add(47.0875 to 2.4020) // Rue Saint-Bonnet
            if (s19 != null) points.add(s19.latitude to s19.longitude)
        }
        3 -> { // 3. Le Grand Œuvre Alchimique & Fulcanelli (sites: 3, 1, 11, 20, 10)
            val s3 = routeSites.find { it.id == 3 }   // Hôtel Lallemant
            val s1 = routeSites.find { it.id == 1 }   // Cathédrale
            val s11 = routeSites.find { it.id == 11 } // Rue Bourbonnoux
            val s20 = routeSites.find { it.id == 20 } // Porte des Étoiles
            val s10 = routeSites.find { it.id == 10 } // Tour des Échevins
            
            if (s3 != null) points.add(s3.latitude to s3.longitude)
            points.add(47.0835 to 2.3975) // Rue Moyenne vers Cathédrale
            if (s1 != null) points.add(s1.latitude to s1.longitude)
            points.add(47.0822 to 2.3998) // Rue Bourbonnoux Sud
            if (s11 != null) points.add(s11.latitude to s11.longitude)
            points.add(47.0805 to 2.4005) // Enceinte Haute
            if (s20 != null) points.add(s20.latitude to s20.longitude)
            points.add(47.0820 to 2.3980) // Remontée vers Place Planchat
            points.add(47.0838 to 2.3960) // Rue des Arènes
            if (s10 != null) points.add(s10.latitude to s10.longitude)
        }
        4 -> { // 4. Sabbats, Sorcellerie & Meneurs de Loups (sites: 12, 16, 14, 5)
            val s12 = routeSites.find { it.id == 12 } // Place Gordaine
            val s16 = routeSites.find { it.id == 16 } // Maison de la Sorcière (Rue Mirebeau)
            val s14 = routeSites.find { it.id == 14 } // Halle au Blé
            val s5 = routeSites.find { it.id == 5 }   // Les Marais
            
            if (s12 != null) points.add(s12.latitude to s12.longitude)
            points.add(47.0860 to 2.3965) // Rue Mirebeau Est
            if (s16 != null) points.add(s16.latitude to s16.longitude)
            points.add(47.0868 to 2.3948) // Rue Coursarlon
            if (s14 != null) points.add(s14.latitude to s14.longitude)
            points.add(47.0880 to 2.4010) // Boulevard Gambetta
            if (s5 != null) points.add(s5.latitude to s5.longitude)
        }
        5 -> { // 5. L'Ombre des Templiers & les Ordres Secrets (sites: 17, 9, 18, 8)
            val s17 = routeSites.find { it.id == 17 } // Prieuré Saint-Martin
            val s9 = routeSites.find { it.id == 9 }   // Église Notre-Dame
            val s18 = routeSites.find { it.id == 18 } // Grange des Dîmes
            val s8 = routeSites.find { it.id == 8 }   // Remparts Gallo-Romains
            
            if (s17 != null) points.add(s17.latitude to s17.longitude)
            points.add(47.0865 to 2.3962) // Rue Notre-Dame
            if (s9 != null) points.add(s9.latitude to s9.longitude)
            points.add(47.0875 to 2.3920) // Rue de la Halle
            points.add(47.0895 to 2.3890) // Vers quartier Célestins
            if (s18 != null) points.add(s18.latitude to s18.longitude)
            points.add(47.0850 to 2.3950) // Traversée médiévale
            points.add(47.0830 to 2.4000) // Descente vers la muraille
            if (s8 != null) points.add(s8.latitude to s8.longitude)
        }
        6 -> { // 6. Le Bourges Souterrain & Carrières d'Avaricum (sites: 15, 7, 8, 18)
            val s15 = routeSites.find { it.id == 15 } // Château d'Eau
            val s7 = routeSites.find { it.id == 7 }   // Crypte
            val s8 = routeSites.find { it.id == 8 }   // Remparts Gallo-Romains
            val s18 = routeSites.find { it.id == 18 } // Grange des Dîmes
            
            if (s15 != null) points.add(s15.latitude to s15.longitude)
            points.add(47.0795 to 2.3965) // Place Séraucourt
            points.add(47.0805 to 2.3985) // Enceinte cathédrale
            if (s7 != null) points.add(s7.latitude to s7.longitude)
            points.add(47.0812 to 2.4005) // Descente des fossés romains
            if (s8 != null) points.add(s8.latitude to s8.longitude)
            points.add(47.0850 to 2.3980) // Rue Bourbonnoux
            points.add(47.0880 to 2.3910) // Vers la Grange des Dîmes
            if (s18 != null) points.add(s18.latitude to s18.longitude)
        }
        7 -> { // 7. Renaissance Flamboyante, Arts & Officines (sites: 6, 11, 10, 3)
            val s6 = routeSites.find { it.id == 6 }   // Hôtel Cujas (Musée du Berry)
            val s11 = routeSites.find { it.id == 11 } // Rue Bourbonnoux
            val s10 = routeSites.find { it.id == 10 } // Tour des Échevins
            val s3 = routeSites.find { it.id == 3 }   // Hôtel Lallemant
            
            if (s6 != null) points.add(s6.latitude to s6.longitude)
            points.add(47.0842 to 2.3970) // Rue des Arènes
            if (s11 != null) points.add(s11.latitude to s11.longitude)
            points.add(47.0838 to 2.3960) // Rue Édouard Vaillant
            if (s10 != null) points.add(s10.latitude to s10.longitude)
            points.add(47.0845 to 2.3962) // Passage des Échevins
            if (s3 != null) points.add(s3.latitude to s3.longitude)
        }
        8 -> { // 8. Secrets Stellaires & L'Alignement Solsticial (sites: 4, 1, 20, 13)
            val s4 = routeSites.find { it.id == 4 }   // Horloge Astronomique
            val s1 = routeSites.find { it.id == 1 }   // Cathédrale
            val s20 = routeSites.find { it.id == 20 } // Porte des Étoiles
            val s13 = routeSites.find { it.id == 13 } // Jardin de l'Archevêché
            
            if (s4 != null) points.add(s4.latitude to s4.longitude)
            if (s1 != null) points.add(s1.latitude to s1.longitude)
            points.add(47.0798 to 2.4000) // Rue Porte Jaune
            if (s20 != null) points.add(s20.latitude to s20.longitude)
            points.add(47.0800 to 2.4005) // Allée du Méridien
            if (s13 != null) points.add(s13.latitude to s13.longitude)
        }
        else -> {
            // Generates street segments following road intersections
            for (idx in 0 until routeSites.size - 1) {
                val current = routeSites[idx]
                val next = routeSites[idx + 1]
                points.add(current.latitude to current.longitude)
                points.add(current.latitude to next.longitude)
            }
            routeSites.lastOrNull()?.let { points.add(it.latitude to it.longitude) }
        }
    }
    return points
}

@Composable
fun InteractiveBourgesMap(
    sites: List<Site>,
    selectedSite: Site?,
    selectedRoute: TourRoute?,
    onSiteSelected: (Site) -> Unit,
    getNormalizedCoords: (Double, Double) -> Pair<Float, Float>,
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null,
    onMyLocationClick: (() -> Unit)? = null,
    onMapTapped: ((Double, Double) -> Unit)? = null,
    cachedTiles: List<com.example.data.OfflineMapData> = emptyList(),
    isOfflineMapEnabled: Boolean = true,
    onCacheOfflineMapClick: (() -> Unit)? = null,
    onToggleOfflineMap: ((Boolean) -> Unit)? = null
) {
    var isOpenStreetMapMode by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedViewPoint by remember { mutableStateOf<MapViewPoint?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val animatedRouteProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedRoute) {
        if (selectedRoute != null) {
            animatedRouteProgress.snapTo(0f)
            animatedRouteProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(1500, easing = LinearEasing)
            )
            // Automatically clear scenic viewpoint selection when route changes
            selectedViewPoint = null
        }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val waterColor = if (isDark) Color(0xFF1E3A5F) else Color(0xFFB3E5FC)
    val wallColor = if (isDark) Color(0xFF37474F) else Color(0xFFCFD8DC)
    val streetColor = if (isDark) Color(0xFF263238) else Color(0xFFEEEEEE)
    val gridColor = if (isDark) Color(0xFF1B2A40) else Color(0xFFF2EFE9)

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .testTag("interactive_map_container")
    ) {
        if (isOpenStreetMapMode) {
            OsmMapViewContent(
                sites = sites,
                selectedSite = selectedSite,
                selectedRoute = selectedRoute,
                onSiteSelected = onSiteSelected,
                userLocation = userLocation,
                onMyLocationClick = onMyLocationClick,
                onMapTapped = onMapTapped,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sites) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            // Check clicks on viewpoints first (scenic priority)
                            var clickedVp: MapViewPoint? = null
                            var minVpDist = Float.MAX_VALUE

                            for (vp in bestViewPoints) {
                                val (nx, ny) = getNormalizedCoords(vp.latitude, vp.longitude)
                                val vpX = nx * width * zoomLevel + panOffset.x
                                val vpY = ny * height * zoomLevel + panOffset.y

                                val dist = sqrt((tapOffset.x - vpX) * (tapOffset.x - vpX) + (tapOffset.y - vpY) * (tapOffset.y - vpY))
                                if (dist < 28.dp.toPx() && dist < minVpDist) {
                                    minVpDist = dist
                                    clickedVp = vp
                                }
                            }

                            if (clickedVp != null) {
                                selectedViewPoint = clickedVp
                                // Also trigger selection of the associated site for a richer experience
                                val associatedSite = sites.find { it.id == clickedVp.associatedSiteId }
                                if (associatedSite != null) {
                                    onSiteSelected(associatedSite)
                                }
                                return@detectTapGestures
                            }

                            // Otherwise check sites
                            var minDistance = Float.MAX_VALUE
                            var closestSite: Site? = null

                            for (site in sites) {
                                val (nx, ny) = getNormalizedCoords(site.latitude, site.longitude)
                                val siteX = nx * width * zoomLevel + panOffset.x
                                val siteY = ny * height * zoomLevel + panOffset.y

                                val dist = sqrt((tapOffset.x - siteX) * (tapOffset.x - siteX) + (tapOffset.y - siteY) * (tapOffset.y - siteY))
                                if (dist < 32.dp.toPx() && dist < minDistance) {
                                    minDistance = dist
                                    closestSite = site
                                }
                            }

                            if (closestSite != null) {
                                onSiteSelected(closestSite)
                                selectedViewPoint = null
                            } else if (onMapTapped != null) {
                                selectedViewPoint = null
                                val nx = ((tapOffset.x - panOffset.x) / (width * zoomLevel)).coerceIn(0f, 1f)
                                val ny = ((tapOffset.y - panOffset.y) / (height * zoomLevel)).coerceIn(0f, 1f)

                                val minLat = 47.0800
                                val maxLat = 47.0895
                                val minLng = 2.3880
                                val maxLng = 2.4060

                                val lng = minLng + nx * (maxLng - minLng)
                                val lat = minLat + (1.0f - ny) * (maxLat - minLat)

                                onMapTapped(lat.toDouble(), lng.toDouble())
                            }
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Grid background
            drawRect(
                color = gridColor,
                size = size
            )

            // Draw coordinate grid lines
            val numGridLines = 8
            for (i in 1..numGridLines) {
                val gridX = w * (i.toFloat() / (numGridLines + 1))
                val gridY = h * (i.toFloat() / (numGridLines + 1))
                drawLine(
                    color = if (isDark) Color(0x1F757575) else Color(0x1F8D6E63),
                    start = Offset(gridX, 0f),
                    end = Offset(gridX, h),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (isDark) Color(0x1F757575) else Color(0x1F8D6E63),
                    start = Offset(0f, gridY),
                    end = Offset(w, gridY),
                    strokeWidth = 1f
                )
            }

            // 2. Draw L'Yèvre River
            val riverPath = Path().apply {
                moveTo(0f, h * 0.15f * zoomLevel + panOffset.y)
                cubicTo(
                    w * 0.3f * zoomLevel + panOffset.x, h * 0.05f * zoomLevel + panOffset.y,
                    w * 0.6f * zoomLevel + panOffset.x, h * 0.35f * zoomLevel + panOffset.y,
                    w, h * 0.2f * zoomLevel + panOffset.y
                )
            }
            drawPath(
                path = riverPath,
                color = waterColor,
                style = Stroke(width = 12.dp.toPx() * zoomLevel, cap = StrokeCap.Round)
            )

            // Wetlands (Les Marais) area
            drawCircle(
                color = waterColor.copy(alpha = 0.4f),
                radius = 45.dp.toPx() * zoomLevel,
                center = Offset(w * 0.8f * zoomLevel + panOffset.x, h * 0.22f * zoomLevel + panOffset.y)
            )

            // 3. Draw Ancient Ramparts
            val rempartCenter = Offset(w * 0.45f * zoomLevel + panOffset.x, h * 0.55f * zoomLevel + panOffset.y)
            drawCircle(
                color = wallColor,
                radius = 85.dp.toPx() * zoomLevel,
                center = rempartCenter,
                style = Stroke(
                    width = 4.dp.toPx() * zoomLevel,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )

            // 4. Draw Medieval Streets
            drawLine(
                color = streetColor,
                start = Offset(w * 0.15f * zoomLevel + panOffset.x, h * 0.45f * zoomLevel + panOffset.y),
                end = Offset(w * 0.75f * zoomLevel + panOffset.x, h * 0.72f * zoomLevel + panOffset.y),
                strokeWidth = 5.dp.toPx() * zoomLevel,
                cap = StrokeCap.Round
            )
            drawLine(
                color = streetColor,
                start = Offset(w * 0.25f * zoomLevel + panOffset.x, h * 0.5f * zoomLevel + panOffset.y),
                end = Offset(w * 0.35f * zoomLevel + panOffset.x, h * 0.95f * zoomLevel + panOffset.y),
                strokeWidth = 4.dp.toPx() * zoomLevel,
                cap = StrokeCap.Round
            )

            // 4.5 Draw Room Cached Map Zones (Offline Vector Data)
            if (isOfflineMapEnabled && cachedTiles.isNotEmpty()) {
                for (tile in cachedTiles) {
                    val (minX, minY) = getNormalizedCoords(tile.maxLat, tile.minLng)
                    val (maxX, maxY) = getNormalizedCoords(tile.minLat, tile.maxLng)
                    val rx = minX * w * zoomLevel + panOffset.x
                    val ry = minY * h * zoomLevel + panOffset.y
                    val rw = (maxX - minX) * w * zoomLevel
                    val rh = (maxY - minY) * h * zoomLevel

                    if (rw > 0 && rh > 0) {
                        drawRoundRect(
                            color = Color(0x224CAF50),
                            topLeft = Offset(rx, ry),
                            size = Size(rw, rh),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color(0xFF4CAF50),
                            topLeft = Offset(rx, ry),
                            size = Size(rw, rh),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            )
                        )
                    }
                }
            }

            // 5. Draw Tour Routes following existing streets realistically
            if (selectedRoute != null) {
                val routeSites = selectedRoute.siteIds.mapNotNull { id -> sites.find { it.id == id } }
                if (routeSites.isNotEmpty()) {
                    val routeColor = Color(android.graphics.Color.parseColor(selectedRoute.colorHex))
                    val streetPoints = getStreetPathPoints(selectedRoute.id, routeSites)
                    if (streetPoints.size >= 2) {
                        val fullPath = Path()
                        var first = true

                        for ((pLat, pLng) in streetPoints) {
                            val (nx, ny) = getNormalizedCoords(pLat, pLng)
                            val sx = nx * w * zoomLevel + panOffset.x
                            val sy = ny * h * zoomLevel + panOffset.y
                            if (first) {
                                fullPath.moveTo(sx, sy)
                                first = false
                            } else {
                                fullPath.lineTo(sx, sy)
                            }
                        }

                        drawPath(
                            path = fullPath,
                            color = routeColor.copy(alpha = 0.85f),
                            style = Stroke(
                                width = 3.5.dp.toPx() * zoomLevel,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(20f, 10f),
                                    animatedRouteProgress.value * -60f
                                ),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            // 5.5 Draw Scenic Viewpoints on Map
            // Only draw viewpoints that are relevant to the selected route (or all if none selected)
            val vpsToDraw = if (selectedRoute != null) {
                bestViewPoints.filter { selectedRoute.siteIds.contains(it.associatedSiteId) }
            } else {
                bestViewPoints
            }

            for (vp in vpsToDraw) {
                val (nx, ny) = getNormalizedCoords(vp.latitude, vp.longitude)
                val vx = nx * w * zoomLevel + panOffset.x
                val vy = ny * h * zoomLevel + panOffset.y

                val isVpSelected = selectedViewPoint == vp

                // Draw a pulsing indicator ring around selected viewpoint
                if (isVpSelected) {
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 18.dp.toPx() * zoomLevel,
                        center = Offset(vx, vy),
                        alpha = pulseAlpha
                    )
                }

                // Draw a cute gold camera symbol manually in Canvas (highly robust and safe!)
                // Base structure
                val camW = 18.dp.toPx() * zoomLevel
                val camH = 12.dp.toPx() * zoomLevel
                drawRoundRect(
                    color = if (isVpSelected) Color(0xFFFFC107) else Color(0xFFFFD54F),
                    topLeft = Offset(vx - camW / 2f, vy - camH / 2f + 1.dp.toPx()),
                    size = Size(camW, camH),
                    cornerRadius = CornerRadius(2.dp.toPx() * zoomLevel, 2.dp.toPx() * zoomLevel)
                )
                // Lens ring
                drawCircle(
                    color = Color(0xFF1E3A5F),
                    radius = 3.5.dp.toPx() * zoomLevel,
                    center = Offset(vx, vy + 1.dp.toPx())
                )
                // Lens reflection
                drawCircle(
                    color = Color.White,
                    radius = 1.dp.toPx() * zoomLevel,
                    center = Offset(vx + 1.5f.dp.toPx(), vy - 0.5f.dp.toPx())
                )
                // Camera shutter button
                drawRect(
                    color = Color(0xFFFF5722),
                    topLeft = Offset(vx - 5.dp.toPx() * zoomLevel, vy - camH / 2f - 1.dp.toPx() * zoomLevel),
                    size = Size(3.dp.toPx() * zoomLevel, 2.dp.toPx() * zoomLevel)
                )
            }

            // 5.6 Draw User GPS Location Dot if within Bourges bounds
            userLocation?.let { (uLat, uLng) ->
                val (nx, ny) = getNormalizedCoords(uLat, uLng)
                if (nx in 0f..1f && ny in 0f..1f) {
                    val ux = nx * w * zoomLevel + panOffset.x
                    val uy = ny * h * zoomLevel + panOffset.y

                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 20.dp.toPx() * zoomLevel,
                        center = Offset(ux, uy),
                        alpha = pulseAlpha * 0.4f
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx() * zoomLevel,
                        center = Offset(ux, uy)
                    )

                    drawCircle(
                        color = Color(0xFF1976D2),
                        radius = 5.dp.toPx() * zoomLevel,
                        center = Offset(ux, uy)
                    )
                }
            }

            // 6. Draw Site Pins
            for (site in sites) {
                val (nx, ny) = getNormalizedCoords(site.latitude, site.longitude)
                val sx = nx * w * zoomLevel + panOffset.x
                val sy = ny * h * zoomLevel + panOffset.y

                val isSelected = selectedSite?.id == site.id
                val siteColor = when (site.category) {
                    "CATHEDRAL" -> Color(0xFFC5A059) // Gold
                    "PALACE" -> Color(0xFF4A154B)    // Royal Purple
                    "NATURE" -> Color(0xFF1E4D2B)    // Green
                    "MUSEUM" -> Color(0xFFE65100)    // Amber Orange
                    else -> primaryColor              // Blue for custom
                }

                if (isSelected) {
                    drawCircle(
                        color = siteColor,
                        radius = 20.dp.toPx() * zoomLevel,
                        center = Offset(sx, sy),
                        alpha = pulseAlpha
                    )
                }

                drawCircle(
                    color = siteColor,
                    radius = if (isSelected) 10.dp.toPx() * zoomLevel else 7.dp.toPx() * zoomLevel,
                    center = Offset(sx, sy)
                )

                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 5.dp.toPx() * zoomLevel else 3.dp.toPx() * zoomLevel,
                    center = Offset(sx, sy)
                )

                if (zoomLevel >= 1.2f || isSelected) {
                    val textStyle = TextStyle(
                        color = if (isDark) Color.White else RegalBlue,
                        fontSize = if (isSelected) 11.sp else 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    val labelResult = textMeasurer.measure(
                        text = site.title,
                        style = textStyle
                    )
                    val textOffset = Offset(
                        sx - labelResult.size.width / 2f,
                        sy - labelResult.size.height - 8.dp.toPx() * zoomLevel
                    )
                    drawRoundRect(
                        color = if (isDark) Color(0xFF0A1523).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(textOffset.x - 4.dp.toPx(), textOffset.y - 2.dp.toPx()),
                        size = Size(labelResult.size.width.toFloat() + 8.dp.toPx(), labelResult.size.height.toFloat() + 4.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = siteColor.copy(alpha = 0.5f),
                        topLeft = Offset(textOffset.x - 4.dp.toPx(), textOffset.y - 2.dp.toPx()),
                        size = Size(labelResult.size.width.toFloat() + 8.dp.toPx(), labelResult.size.height.toFloat() + 4.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 1f)
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = textOffset
                    )
                }
            }

            // Compass Rose
            val compassCenter = Offset(w - 36.dp.toPx(), 36.dp.toPx())
            drawCircle(
                color = if (isDark) Color(0x3FCCCCCC) else Color(0x3F1A365D),
                radius = 16.dp.toPx(),
                center = compassCenter,
                style = Stroke(width = 1.dp.toPx())
            )
            drawLine(
                color = Color.Red,
                start = compassCenter,
                end = Offset(compassCenter.x, compassCenter.y - 12.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = if (isDark) Color.White else RegalBlue,
                start = compassCenter,
                end = Offset(compassCenter.x, compassCenter.y + 12.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Selected Scenic Viewpoint Details Popover Card
        selectedViewPoint?.let { vp ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
                    .fillMaxWidth(0.95f)
                    .testTag("scenic_viewpoint_overlay"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📸", fontSize = 16.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vp.titleFr,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = vp.descriptionFr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(
                        onClick = { selectedViewPoint = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("×", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        // GPS & Zoom Controls Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My Location Button
            if (onMyLocationClick != null) {
                IconButton(
                    onClick = onMyLocationClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .testTag("my_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Ma position",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zoom Overlay
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f)
                        if (zoomLevel <= 1.0f) panOffset = Offset.Zero
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${(zoomLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(3.0f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    FilterChip(
                        selected = isOpenStreetMapMode,
                        onClick = { isOpenStreetMapMode = true },
                        label = { Text("OpenStreetMap 🗺️", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(26.dp)
                    )
                    FilterChip(
                        selected = !isOpenStreetMapMode,
                        onClick = { isOpenStreetMapMode = false },
                        label = { Text("Illustrée 🎨", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            val cacheSizeKb = cachedTiles.sumOf { it.sizeBytes } / 1024
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (cachedTiles.isNotEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("offline_map_cache_badge")
                    .then(if (onCacheOfflineMapClick != null) Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF2E7D32)) else Modifier)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (cachedTiles.isNotEmpty()) "💾 Room: ${cachedTiles.size} zones (${cacheSizeKb} KB)" else "💾 Room: Non cachée",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                    )
                    if (onCacheOfflineMapClick != null) {
                        IconButton(
                            onClick = onCacheOfflineMapClick,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Text("🔄", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OsmMapViewContent(
    sites: List<Site>,
    selectedSite: Site?,
    selectedRoute: TourRoute?,
    onSiteSelected: (Site) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null,
    onMyLocationClick: (() -> Unit)? = null,
    onMapTapped: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var selectedTileSourceIndex by remember { mutableIntStateOf(0) }

    val tileSources = listOf(
        TileSourceFactory.MAPNIK to "Standard 🗺️",
        TileSourceFactory.USGS_SAT to "Satellite 🛰️",
        TileSourceFactory.USGS_TOPO to "Relief ⛰️"
    )

    val bourgesCenter = GeoPoint(47.0810, 2.3980)

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
        )
    }

    LaunchedEffect(selectedSite?.id) {
        if (selectedSite != null && mapViewRef != null) {
            val targetPoint = GeoPoint(selectedSite.latitude, selectedSite.longitude)
            mapViewRef?.controller?.animateTo(targetPoint, 16.5, 800L)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                Configuration.getInstance().load(
                    ctx,
                    ctx.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
                )
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.2)
                    controller.setCenter(bourgesCenter)

                    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onMapTapped?.invoke(p.latitude, p.longitude)
                            return false
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    })
                    overlays.add(0, mapEventsOverlay)

                    mapViewRef = this
                }
            },
            update = { mapView ->
                mapView.setTileSource(tileSources[selectedTileSourceIndex].first)

                // Preserve MapEventsOverlay, clear others
                val eventsOverlay = mapView.overlays.firstOrNull { it is MapEventsOverlay }
                mapView.overlays.clear()
                if (eventsOverlay != null) {
                    mapView.overlays.add(eventsOverlay)
                }

                // Selected Route Polyline
                if (selectedRoute != null) {
                    val pathPoints = getStreetPathPoints(selectedRoute.id, sites)
                    val geoPoints = pathPoints.map { GeoPoint(it.first, it.second) }
                    val polyline = Polyline(mapView).apply {
                        setPoints(geoPoints)
                        outlinePaint.color = android.graphics.Color.parseColor("#D4AF37")
                        outlinePaint.strokeWidth = 12f
                    }
                    mapView.overlays.add(polyline)
                }

                // Scenic Viewpoints Markers
                bestViewPoints.forEach { vp ->
                    val vpPoint = GeoPoint(vp.latitude, vp.longitude)
                    val marker = Marker(mapView).apply {
                        position = vpPoint
                        title = vp.titleFr
                        snippet = vp.descriptionFr
                        icon = MapMarkerFactory.getViewPointMarker(context)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            val assoc = sites.find { s -> s.id == vp.associatedSiteId }
                            if (assoc != null) {
                                onSiteSelected(assoc)
                            }
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                // Sites Markers
                sites.forEach { site ->
                    val sitePoint = GeoPoint(site.latitude, site.longitude)
                    val isSelected = site.id == selectedSite?.id
                    val marker = Marker(mapView).apply {
                        position = sitePoint
                        title = site.title
                        snippet = site.category
                        icon = MapMarkerFactory.getSiteMarker(context, site.category, isSelected)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        if (isSelected) {
                            showInfoWindow()
                        }
                        setOnMarkerClickListener { _, _ ->
                            onSiteSelected(site)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                // User Location Marker
                if (userLocation != null) {
                    val userPoint = GeoPoint(userLocation.first, userLocation.second)
                    val userMarker = Marker(mapView).apply {
                        position = userPoint
                        title = "Ma position GPS"
                        icon = MapMarkerFactory.getUserLocationMarker(context)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    mapView.overlays.add(userMarker)
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Style Selector Overlay (Top End)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tileSources.forEachIndexed { idx, item ->
                FilterChip(
                    selected = selectedTileSourceIndex == idx,
                    onClick = { selectedTileSourceIndex = idx },
                    label = { Text(item.second, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }

        // Floating Action Buttons for Zoom In/Out & My Location (Bottom End)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onMyLocationClick != null && userLocation != null) {
                FloatingActionButton(
                    onClick = {
                        onMyLocationClick()
                        mapViewRef?.controller?.animateTo(GeoPoint(userLocation.first, userLocation.second), 16.5, 800L)
                    },
                    modifier = Modifier.size(36.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Ma position",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Column {
                    IconButton(
                        onClick = {
                            mapViewRef?.controller?.zoomIn()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.width(32.dp))
                    IconButton(
                        onClick = {
                            mapViewRef?.controller?.zoomOut()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

