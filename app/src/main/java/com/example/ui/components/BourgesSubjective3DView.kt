package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Site
import com.example.data.TourRoute
import com.example.ui.theme.RegalBlue
import com.example.ui.theme.SandstoneGold
import com.example.ui.util.BourgesIllustrationHelper
import kotlin.math.*

/**
 * Navigation maneuver instruction for pedestrian street following in Bourges.
 */
enum class ManeuverDirection {
    DEPARTURE,
    STRAIGHT,
    TURN_RIGHT,
    TURN_LEFT,
    SLIGHT_RIGHT,
    SLIGHT_LEFT,
    ARRIVAL
}

/**
 * Detailed step in a tour with real Bourges street names, distance and turn instructions.
 */
data class TourNavigationLeg(
    val stepIndex: Int,
    val site: Site,
    val streetNameFr: String,
    val streetNameEn: String,
    val instructionFr: String,
    val instructionEn: String,
    val distanceMeters: Int,
    val durationMinutes: Int,
    val maneuver: ManeuverDirection,
    val waypoints: List<Pair<Double, Double>> // Detailed intermediate street coordinates
)

/**
 * Helper to produce realistic street-following navigation legs for every tour route in Bourges.
 */
object BourgesNavigationRouter {

    fun buildNavigationLegs(route: TourRoute, sites: List<Site>): List<TourNavigationLeg> {
        val routeSites = route.siteIds.mapNotNull { id -> sites.find { it.id == id } }
        if (routeSites.isEmpty()) return emptyList()

        val legs = mutableListOf<TourNavigationLeg>()

        for (i in routeSites.indices) {
            val currentSite = routeSites[i]
            val isFirst = i == 0
            val isLast = i == routeSites.size - 1

            val (streetNameFr, streetNameEn, instructionFr, instructionEn, maneuver, distanceMeters) = when (route.id) {
                1 -> when (i) { // L'Athanor et le Cœur d'Or (Cathédrale -> Horloge -> Crypte -> Palais Jacques Cœur)
                    0 -> LegInfo("Place Étienne Dolet", "Place Etienne Dolet", "Départ au pied de la Cathédrale Saint-Étienne", "Start at Saint-Etienne Cathedral", ManeuverDirection.DEPARTURE, 0)
                    1 -> LegInfo("Nef Nord de la Cathédrale", "Cathedral North Nave", "Avancer de 40 m vers l'Horloge Astronomique de Jean Fusoris", "Walk 40 m to the Astronomical Clock", ManeuverDirection.STRAIGHT, 45)
                    2 -> LegInfo("Chœur & Déambulatoire", "Cathedral Choir", "Descendre les marches vers la Crypte Romane et Gothique", "Walk down the steps towards the Crypt", ManeuverDirection.SLIGHT_RIGHT, 60)
                    else -> LegInfo("Rue Moyenne & Rue Jacques Cœur", "Rue Moyenne & Jacques Coeur", "Emprunter la Rue des Armuriers puis descendre la Rue Jacques Cœur vers le Palais", "Take Rue des Armuriers down to Jacques Coeur Palace", ManeuverDirection.TURN_RIGHT, 450)
                }
                2 -> when (i) { // L'Échappée Verte (Archevêché -> Marais -> Saint-Bonnet)
                    0 -> LegInfo("Jardin de l'Archevêché", "Archbishop's Garden", "Départ sous les marronniers du Jardin de l'Archevêché", "Departure at Archbishop's Garden", ManeuverDirection.DEPARTURE, 0)
                    1 -> LegInfo("Passerelle de la Voiselle", "Voiselle Footbridge", "Descendre la Rue Branly et traverser le canal vers les Marais", "Head down Rue Branly and cross the canal into the marshes", ManeuverDirection.TURN_LEFT, 520)
                    else -> LegInfo("Rue Saint-Bonnet", "Rue Saint-Bonnet", "Remonter le quai du canal vers l'Église Saint-Bonnet", "Follow canal quai up to Saint-Bonnet Church", ManeuverDirection.TURN_RIGHT, 380)
                }
                3 -> when (i) { // Alchimie & Fulcanelli (Hôtel Lallemant -> Cathédrale -> Rue Bourbonnoux -> Porte des Étoiles -> Tour des Échevins)
                    0 -> LegInfo("Rue Bourbonnoux", "Rue Bourbonnoux", "Départ devant l'Hôtel Lallemant, sanctuaire des alchimistes", "Start at Hotel Lallemant", ManeuverDirection.DEPARTURE, 0)
                    1 -> LegInfo("Rue Moyenne", "Rue Moyenne", "Remonter vers le parvis de la Cathédrale Saint-Étienne", "Walk up towards the Cathedral forecourt", ManeuverDirection.STRAIGHT, 310)
                    2 -> LegInfo("Rue Bourbonnoux Haute", "High Bourbonnoux Street", "Suivre la ruelle pavée bordée de maisons médiévales", "Follow the cobblestone street with medieval houses", ManeuverDirection.TURN_LEFT, 190)
                    3 -> LegInfo("Allée du Méridien", "Meridian Alley", "Passer sous la Porte des Étoiles près des remparts", "Pass under Star Gate near ancient ramparts", ManeuverDirection.STRAIGHT, 260)
                    else -> LegInfo("Rue des Arènes", "Rue des Arenes", "Traverser la Place Planchat vers la Tour des Échevins", "Cross Planchat Square to Echevins Tower", ManeuverDirection.TURN_RIGHT, 340)
                }
                else -> {
                    val prevName = if (i > 0) routeSites[i - 1].title else "Départ"
                    val dist = 250 + (i * 70)
                    LegInfo(
                        streetNameFr = "Rues médiévales de Bourges",
                        streetNameEn = "Bourges Medieval Streets",
                        instructionFr = if (isFirst) "Point de départ du parcours : ${currentSite.title}" else if (isLast) "Dernière étape : Arrivée à ${currentSite.title}" else "Suivre l'itinéraire fléché vers ${currentSite.title}",
                        instructionEn = if (isFirst) "Start point: ${currentSite.title}" else "Proceed to ${currentSite.title}",
                        maneuver = if (isFirst) ManeuverDirection.DEPARTURE else if (isLast) ManeuverDirection.ARRIVAL else if (i % 2 == 0) ManeuverDirection.TURN_RIGHT else ManeuverDirection.TURN_LEFT,
                        distanceMeters = if (isFirst) 0 else dist
                    )
                }
            }

            val durationMin = max(1, (distanceMeters / 70.0).roundToInt())

            // Intermediate waypoints for smooth 3D street animation
            val waypoints = mutableListOf<Pair<Double, Double>>()
            if (i > 0) {
                val prev = routeSites[i - 1]
                waypoints.add(prev.latitude to prev.longitude)
                // Midpoint street corner
                val midLat = (prev.latitude + currentSite.latitude) / 2.0
                val midLng = (prev.longitude + currentSite.longitude) / 2.0
                waypoints.add(midLat to midLng)
            }
            waypoints.add(currentSite.latitude to currentSite.longitude)

            legs.add(
                TourNavigationLeg(
                    stepIndex = i + 1,
                    site = currentSite,
                    streetNameFr = streetNameFr,
                    streetNameEn = streetNameEn,
                    instructionFr = instructionFr,
                    instructionEn = instructionEn,
                    distanceMeters = distanceMeters,
                    durationMinutes = durationMin,
                    maneuver = maneuver,
                    waypoints = waypoints
                )
            )
        }

        return legs
    }

    private data class LegInfo(
        val streetNameFr: String,
        val streetNameEn: String,
        val instructionFr: String,
        val instructionEn: String,
        val maneuver: ManeuverDirection,
        val distanceMeters: Int
    )
}

/**
 * 3D Subjective Point-of-View GPS Navigation Composable.
 * Features:
 * - Real-time first-person street perspective with vanishing point
 * - Cobblestone road ribbon with illuminated guide arrows pointing to the next destination
 * - 3D extruded OSM building facades and medieval half-timbered architecture of Bourges
 * - Iconic 3D silhouettes (Cathédrale towers, Palais Jacques Cœur spires)
 * - Floating 3D POI target pin with distance indicator
 * - Turn-by-turn navigation HUD (Google Maps style)
 * - Interactive walking simulation and camera pan controls
 */
@Composable
fun BourgesSubjective3DView(
    route: TourRoute,
    sites: List<Site>,
    currentStepIndex: Int,
    onStepSelected: (Int) -> Unit,
    onSiteClick: (Site) -> Unit,
    onClose3D: () -> Unit,
    langCode: String = "FR",
    modifier: Modifier = Modifier
) {
    val navigationLegs = remember(route, sites) {
        BourgesNavigationRouter.buildNavigationLegs(route, sites)
    }

    val activeStep = navigationLegs.getOrElse(currentStepIndex.coerceIn(0, max(0, navigationLegs.size - 1))) {
        navigationLegs.firstOrNull()
    }

    // Walking animation progress (0f = at previous point, 1f = arrived at current target)
    var walkingProgress by remember(currentStepIndex) { mutableFloatStateOf(0.15f) }
    var isAutoWalking by remember { mutableStateOf(false) }
    var cameraYawDegrees by remember { mutableFloatStateOf(0f) }

    // Pulse animation for the directional guidance path on the ground
    val infiniteTransition = rememberInfiniteTransition(label = "TrailPulse")
    val trailPulseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TrailOffset"
    )

    // Auto walking timer effect
    LaunchedEffect(isAutoWalking, currentStepIndex) {
        if (isAutoWalking) {
            while (walkingProgress < 1.0f) {
                kotlinx.coroutines.delay(80)
                walkingProgress = (walkingProgress + 0.025f).coerceAtMost(1.0f)
            }
            isAutoWalking = false
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C1424))
            .testTag("subjective_3d_view_container")
    ) {
        // 3D Subjective Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        cameraYawDegrees = (cameraYawDegrees + dragAmount.x * 0.18f).coerceIn(-65f, 65f)
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Atmospheric Sky Gradient (Bourges Twilight / Golden Hour)
            val horizonY = height * 0.44f
            val vanishingX = width * 0.5f + (cameraYawDegrees * (width / 200f))

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF081224), // Deep indigo zenith
                        Color(0xFF162D4A), // Twilight blue
                        Color(0xFF4A3B32), // Warm sandstone horizon haze
                        Color(0xFF7D5930)  // Golden hour glow
                    ),
                    startY = 0f,
                    endY = horizonY
                ),
                size = Size(width, horizonY)
            )

            // Celestial Sun/Moon glow on horizon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE082).copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(vanishingX, horizonY - 24.dp.toPx()),
                    radius = 80.dp.toPx()
                ),
                radius = 80.dp.toPx(),
                center = Offset(vanishingX, horizonY - 24.dp.toPx())
            )

            // 2. Distant Monument Silhouettes (Cathédrale Saint-Étienne & Gothic spires)
            drawCathedralSilhouette3D(
                vanishingX = vanishingX,
                horizonY = horizonY,
                targetSiteId = activeStep?.site?.id ?: 1
            )

            // 3. Ground / Cobblestone Street Perspective
            drawGroundAndStreet3D(
                width = width,
                height = height,
                vanishingX = vanishingX,
                horizonY = horizonY,
                walkingProgress = walkingProgress,
                trailPulseOffset = trailPulseOffset
            )

            // 4. Extruded OSM Medieval Facades (Half-timbered buildings along the street corridor)
            drawOsmMedievalBuildings3D(
                width = width,
                height = height,
                vanishingX = vanishingX,
                horizonY = horizonY,
                walkingProgress = walkingProgress
            )

            // 5. Floating Augmented Reality POI Pin at the Destination
            if (activeStep != null) {
                drawFloatingPoiMarker3D(
                    width = width,
                    horizonY = horizonY,
                    vanishingX = vanishingX,
                    activeStep = activeStep,
                    walkingProgress = walkingProgress,
                    langCode = langCode,
                    textMeasurer = textMeasurer
                )
            }
        }

        // Top Header: Navigation Turn-by-Turn HUD (Google Maps Style)
        if (activeStep != null) {
            val remainingMeters = max(10, ((1f - walkingProgress) * activeStep.distanceMeters).roundToInt())
            val remainingMin = max(1, (remainingMeters / 70.0).roundToInt())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Direction card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36).copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, SandstoneGold.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Maneuver Icon Box
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SandstoneGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activeStep.maneuver) {
                                    ManeuverDirection.TURN_RIGHT, ManeuverDirection.SLIGHT_RIGHT -> Icons.Default.TurnRight
                                    ManeuverDirection.TURN_LEFT, ManeuverDirection.SLIGHT_LEFT -> Icons.Default.TurnLeft
                                    ManeuverDirection.ARRIVAL -> Icons.Default.Flag
                                    else -> Icons.Default.Straight
                                },
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (remainingMeters <= 15) "Arrivé à destination !" else "Dans $remainingMeters m",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = if (remainingMeters <= 15) Color(0xFF4CAF50) else Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• $remainingMin min",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = if (langCode == "FR") activeStep.instructionFr else activeStep.instructionEn,
                                fontSize = 13.sp,
                                color = SandstoneGold,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            Text(
                                text = "Étape ${activeStep.stepIndex}/${navigationLegs.size} : ${activeStep.site.getLocalizedTitle(langCode)}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }

                        // Close 3D View button
                        IconButton(
                            onClick = onClose3D,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer vue 3D",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Compass & Pan instruction banner
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "🧭 Vue 3D Subjective • Glisser pour tourner le regard",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Cap: ${((cameraYawDegrees + 360) % 360).roundToInt()}°",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SandstoneGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Bottom Controls: Walk Simulator, Step Switcher, and Arrival Action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            // Arrival Celebration Card when < 20m from destination
            if (activeStep != null && walkingProgress >= 0.95f) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.95f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vous êtes arrivé devant le lieu !",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = activeStep.site.getLocalizedTitle(langCode),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Button(
                            onClick = { onSiteClick(activeStep.site) },
                            colors = ButtonDefaults.buttonColors(containerColor = SandstoneGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Secrets 📜", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Navigation Control Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1424).copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Step Dots Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navigationLegs.forEachIndexed { idx, leg ->
                            val isCompleted = idx < currentStepIndex
                            val isCurrent = idx == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isCurrent) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> SandstoneGold
                                            isCompleted -> Color(0xFF4CAF50)
                                            else -> Color.White.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons Row: Previous, Walk Simulation, Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Step Button
                        OutlinedButton(
                            onClick = {
                                if (currentStepIndex > 0) {
                                    onStepSelected(currentStepIndex - 1)
                                    walkingProgress = 0.1f
                                }
                            },
                            enabled = currentStepIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Précédent", fontSize = 12.sp)
                        }

                        // Walk Button (Advances 25% or triggers auto walk)
                        Button(
                            onClick = {
                                if (walkingProgress < 0.95f) {
                                    walkingProgress = (walkingProgress + 0.25f).coerceAtMost(1.0f)
                                } else if (currentStepIndex < navigationLegs.size - 1) {
                                    onStepSelected(currentStepIndex + 1)
                                    walkingProgress = 0.1f
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (walkingProgress >= 0.95f) Color(0xFF2E7D32) else SandstoneGold,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(
                                imageVector = if (walkingProgress >= 0.95f) Icons.Default.Check else Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (walkingProgress >= 0.95f) "Terminer étape" else "Avancer 🚶",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Next Step Button
                        OutlinedButton(
                            onClick = {
                                if (currentStepIndex < navigationLegs.size - 1) {
                                    onStepSelected(currentStepIndex + 1)
                                    walkingProgress = 0.1f
                                }
                            },
                            enabled = currentStepIndex < navigationLegs.size - 1,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Suivant", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the ground cobblestone perspective and the illuminated 3D navigation guidance trail.
 */
private fun DrawScope.drawGroundAndStreet3D(
    width: Float,
    height: Float,
    vanishingX: Float,
    horizonY: Float,
    walkingProgress: Float,
    trailPulseOffset: Float
) {
    // Ground plane (Dark medieval paving base)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E242B), // Distant street
                Color(0xFF14181E), // Mid street
                Color(0xFF0F1216)  // Foreground street
            ),
            startY = horizonY,
            endY = height
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(width, height - horizonY)
    )

    // Street Ribbon (Trapezoid from horizon to bottom of screen)
    val streetTopWidth = 24.dp.toPx()
    val streetBottomWidth = width * 0.72f

    val streetPath = Path().apply {
        moveTo(vanishingX - streetTopWidth / 2f, horizonY)
        lineTo(vanishingX + streetTopWidth / 2f, horizonY)
        lineTo(width / 2f + streetBottomWidth / 2f, height)
        lineTo(width / 2f - streetBottomWidth / 2f, height)
        close()
    }

    // Street pavement fill
    drawPath(
        path = streetPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C3238),
                Color(0xFF242A30),
                Color(0xFF1E2328)
            ),
            startY = horizonY,
            endY = height
        )
    )

    // Sidewalk Stone Curbs
    val leftCurbPath = Path().apply {
        moveTo(vanishingX - streetTopWidth / 2f, horizonY)
        lineTo(width / 2f - streetBottomWidth / 2f, height)
    }
    val rightCurbPath = Path().apply {
        moveTo(vanishingX + streetTopWidth / 2f, horizonY)
        lineTo(width / 2f + streetBottomWidth / 2f, height)
    }
    drawPath(leftCurbPath, Color(0xFF6B7280), style = Stroke(width = 3.dp.toPx()))
    drawPath(rightCurbPath, Color(0xFF6B7280), style = Stroke(width = 3.dp.toPx()))

    // Cobblestone transverse lines in perspective
    val numRungs = 14
    for (i in 0 until numRungs) {
        val t = (i.toFloat() / numRungs).pow(2.2f) // Perspective compression
        val y = horizonY + (height - horizonY) * t
        val rungWidth = streetTopWidth + (streetBottomWidth - streetTopWidth) * t
        val centerX = vanishingX + (width / 2f - vanishingX) * t

        drawLine(
            color = Color(0xFF374151).copy(alpha = 0.55f * t),
            start = Offset(centerX - rungWidth / 2f, y),
            end = Offset(centerX + rungWidth / 2f, y),
            strokeWidth = (1f + 3f * t).dp.toPx()
        )
    }

    // Illuminated 3D Navigation Path Ribbon (Glowing Neon Cyan / Sandstone Gold Arrow Trail)
    val trailWidth = 28.dp.toPx()
    val numArrows = 7
    for (i in 0 until numArrows) {
        // Animate arrows moving forward into the distance
        val rawT = ((i.toFloat() / numArrows) + trailPulseOffset * (1f / numArrows)) % 1f
        val t = rawT.pow(1.8f) // perspective depth
        val arrowY = horizonY + (height - horizonY) * (1f - t) // Move from foreground to vanishing point
        val scale = (1f - t).coerceIn(0.15f, 1f)

        val arrowCenterX = vanishingX + (width / 2f - vanishingX) * (1f - t)
        val arrowSize = trailWidth * scale

        // Draw chevron arrow pointing forward
        val chevronPath = Path().apply {
            moveTo(arrowCenterX - arrowSize / 2f, arrowY + arrowSize * 0.4f)
            lineTo(arrowCenterX, arrowY - arrowSize * 0.4f)
            lineTo(arrowCenterX + arrowSize / 2f, arrowY + arrowSize * 0.4f)
        }

        // Outer glow
        drawPath(
            path = chevronPath,
            color = Color(0xFF00E5FF).copy(alpha = 0.35f * scale),
            style = Stroke(width = (6f * scale).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Inner bright core
        drawPath(
            path = chevronPath,
            color = Color(0xFFFFD54F).copy(alpha = 0.85f * scale),
            style = Stroke(width = (3f * scale).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Extruded OpenStreetMap building facades along the street corridor (Half-timbered medieval Bourges style).
 */
private fun DrawScope.drawOsmMedievalBuildings3D(
    width: Float,
    height: Float,
    vanishingX: Float,
    horizonY: Float,
    walkingProgress: Float
) {
    // 3 Houses on the Left side, 3 Houses on the Right side
    val leftHouses = listOf(
        HouseProfile(roofColor = Color(0xFF8D4925), wallColor = Color(0xFFD9C3A5), timberColor = Color(0xFF422817), heightFactor = 1.4f),
        HouseProfile(roofColor = Color(0xFF653B24), wallColor = Color(0xFFC7B39B), timberColor = Color(0xFF382215), heightFactor = 1.6f),
        HouseProfile(roofColor = Color(0xFF7A3E1D), wallColor = Color(0xFFE2D4BC), timberColor = Color(0xFF4A2C1A), heightFactor = 1.3f)
    )

    val rightHouses = listOf(
        HouseProfile(roofColor = Color(0xFF5E3929), wallColor = Color(0xFFCEB89E), timberColor = Color(0xFF3D2314), heightFactor = 1.5f),
        HouseProfile(roofColor = Color(0xFF7C4426), wallColor = Color(0xFFDFCFBA), timberColor = Color(0xFF472A1B), heightFactor = 1.35f),
        HouseProfile(roofColor = Color(0xFF6B381E), wallColor = Color(0xFFC9B7A2), timberColor = Color(0xFF381F10), heightFactor = 1.65f)
    )

    val streetBottomWidth = width * 0.72f
    val streetTopWidth = 24.dp.toPx()

    // Render Left Side Facades
    for (i in leftHouses.indices) {
        val tNear = (i + 1).toFloat() / (leftHouses.size + 1)
        val tFar = i.toFloat() / (leftHouses.size + 1)

        val nearY = horizonY + (height - horizonY) * tNear.pow(1.9f)
        val farY = horizonY + (height - horizonY) * tFar.pow(1.9f)

        val nearStreetX = width / 2f - (streetBottomWidth / 2f) * tNear
        val farStreetX = vanishingX - (streetTopWidth / 2f) * (1f - tFar)

        val house = leftHouses[i]
        val houseHeightNear = (height - horizonY) * 0.85f * tNear * house.heightFactor
        val houseHeightFar = (height - horizonY) * 0.85f * tFar * house.heightFactor

        // Facade Polygon
        val facade = Path().apply {
            moveTo(farStreetX, farY)
            lineTo(farStreetX, farY - houseHeightFar)
            // Gable roof peak
            lineTo((farStreetX + nearStreetX) / 2f, (farY + nearY) / 2f - ((houseHeightFar + houseHeightNear) / 2f * 1.25f))
            lineTo(nearStreetX, nearY - houseHeightNear)
            lineTo(nearStreetX, nearY)
            close()
        }

        drawPath(facade, house.wallColor)

        // Half-timbering (pans de bois) beams
        val beamStroke = (1.5f + 2f * tNear).dp.toPx()
        drawLine(house.timberColor, Offset(farStreetX, farY), Offset(nearStreetX, nearY), strokeWidth = beamStroke)
        drawLine(house.timberColor, Offset(farStreetX, farY - houseHeightFar), Offset(nearStreetX, nearY - houseHeightNear), strokeWidth = beamStroke)
        drawLine(house.timberColor, Offset(farStreetX, farY - houseHeightFar * 0.5f), Offset(nearStreetX, nearY - houseHeightNear * 0.5f), strokeWidth = beamStroke)

        // Diagonal braces
        drawLine(house.timberColor, Offset(farStreetX, farY), Offset(nearStreetX, nearY - houseHeightNear * 0.5f), strokeWidth = beamStroke * 0.7f)
        drawLine(house.timberColor, Offset(farStreetX, farY - houseHeightFar * 0.5f), Offset(nearStreetX, nearY), strokeWidth = beamStroke * 0.7f)

        // Renaissance Windows
        if (tNear > 0.4f) {
            val winW = 12.dp.toPx() * tNear
            val winH = 18.dp.toPx() * tNear
            val winX = (farStreetX + nearStreetX) / 2f - winW / 2f
            val winY = (farY + nearY) / 2f - (houseHeightFar + houseHeightNear) / 4f
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(winX, winY),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFFFD54F).copy(alpha = 0.4f), // Warm interior light
                topLeft = Offset(winX + 2.dp.toPx(), winY + 2.dp.toPx()),
                size = Size(winW - 4.dp.toPx(), winH - 4.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }

    // Render Right Side Facades
    for (i in rightHouses.indices) {
        val tNear = (i + 1).toFloat() / (rightHouses.size + 1)
        val tFar = i.toFloat() / (rightHouses.size + 1)

        val nearY = horizonY + (height - horizonY) * tNear.pow(1.9f)
        val farY = horizonY + (height - horizonY) * tFar.pow(1.9f)

        val nearStreetX = width / 2f + (streetBottomWidth / 2f) * tNear
        val farStreetX = vanishingX + (streetTopWidth / 2f) * (1f - tFar)

        val house = rightHouses[i]
        val houseHeightNear = (height - horizonY) * 0.85f * tNear * house.heightFactor
        val houseHeightFar = (height - horizonY) * 0.85f * tFar * house.heightFactor

        val facade = Path().apply {
            moveTo(farStreetX, farY)
            lineTo(farStreetX, farY - houseHeightFar)
            lineTo((farStreetX + nearStreetX) / 2f, (farY + nearY) / 2f - ((houseHeightFar + houseHeightNear) / 2f * 1.25f))
            lineTo(nearStreetX, nearY - houseHeightNear)
            lineTo(nearStreetX, nearY)
            close()
        }

        drawPath(facade, house.wallColor.copy(red = house.wallColor.red * 0.88f)) // Shade opposite side

        val beamStroke = (1.5f + 2f * tNear).dp.toPx()
        drawLine(house.timberColor, Offset(farStreetX, farY), Offset(nearStreetX, nearY), strokeWidth = beamStroke)
        drawLine(house.timberColor, Offset(farStreetX, farY - houseHeightFar), Offset(nearStreetX, nearY - houseHeightNear), strokeWidth = beamStroke)
        drawLine(house.timberColor, Offset(farStreetX, farY - houseHeightFar * 0.5f), Offset(nearStreetX, nearY - houseHeightNear * 0.5f), strokeWidth = beamStroke)

        drawLine(house.timberColor, Offset(nearStreetX, nearY), Offset(farStreetX, farY - houseHeightFar * 0.5f), strokeWidth = beamStroke * 0.7f)
        drawLine(house.timberColor, Offset(nearStreetX, nearY - houseHeightNear * 0.5f), Offset(farStreetX, farY), strokeWidth = beamStroke * 0.7f)

        if (tNear > 0.4f) {
            val winW = 12.dp.toPx() * tNear
            val winH = 18.dp.toPx() * tNear
            val winX = (farStreetX + nearStreetX) / 2f - winW / 2f
            val winY = (farY + nearY) / 2f - (houseHeightFar + houseHeightNear) / 4f
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(winX, winY),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFFFD54F).copy(alpha = 0.4f),
                topLeft = Offset(winX + 2.dp.toPx(), winY + 2.dp.toPx()),
                size = Size(winW - 4.dp.toPx(), winH - 4.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}

private data class HouseProfile(
    val roofColor: Color,
    val wallColor: Color,
    val timberColor: Color,
    val heightFactor: Float
)

/**
 * 3D Monument Silhouettes (Cathédrale towers, spires, and flying buttresses) on the horizon.
 */
private fun DrawScope.drawCathedralSilhouette3D(
    vanishingX: Float,
    horizonY: Float,
    targetSiteId: Int
) {
    val cathColor = Color(0xFF231B15) // Backlit stone silhouette
    val cathHighlight = SandstoneGold.copy(alpha = 0.4f)

    // Center Gothic Cathedral facade rising in the distance
    val cathW = 110.dp.toPx()
    val cathH = 140.dp.toPx()
    val cathLeft = vanishingX - cathW / 2f
    val cathBaseY = horizonY

    // Cathedral body
    val path = Path().apply {
        moveTo(cathLeft, cathBaseY)
        // Left tower (Tour Sourde)
        lineTo(cathLeft + 15.dp.toPx(), cathBaseY - cathH * 0.8f)
        lineTo(cathLeft + 35.dp.toPx(), cathBaseY - cathH * 0.8f)
        lineTo(cathLeft + 35.dp.toPx(), cathBaseY - cathH * 0.6f)
        // Center Gothic Gable & Rose Window apex
        lineTo(vanishingX, cathBaseY - cathH * 0.72f)
        // Right Tower (Tour de Beurre - taller Gothic pinnacle)
        lineTo(cathLeft + cathW - 35.dp.toPx(), cathBaseY - cathH * 0.6f)
        lineTo(cathLeft + cathW - 35.dp.toPx(), cathBaseY - cathH)
        lineTo(cathLeft + cathW - 15.dp.toPx(), cathBaseY - cathH)
        lineTo(cathLeft + cathW, cathBaseY)
        close()
    }

    drawPath(path, cathColor)
    drawPath(path, cathHighlight, style = Stroke(width = 1.5.dp.toPx()))

    // Rose window glow
    drawCircle(
        color = Color(0xFFFFB300).copy(alpha = 0.6f),
        radius = 8.dp.toPx(),
        center = Offset(vanishingX, cathBaseY - cathH * 0.45f)
    )
}

/**
 * Floating 3D Augmented POI Pin hovering above the street destination.
 */
private fun DrawScope.drawFloatingPoiMarker3D(
    width: Float,
    horizonY: Float,
    vanishingX: Float,
    activeStep: TourNavigationLeg,
    walkingProgress: Float,
    langCode: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    // Pin descends and grows as user approaches
    val approachScale = (0.7f + 0.35f * walkingProgress).coerceIn(0.7f, 1.05f)
    val pinX = vanishingX
    val pinY = horizonY - (70.dp.toPx() * approachScale)

    // Glowing vertical tether beam down to ground
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(SandstoneGold, Color.Transparent),
            startY = pinY + 16.dp.toPx() * approachScale,
            endY = horizonY
        ),
        start = Offset(pinX, pinY + 16.dp.toPx() * approachScale),
        end = Offset(pinX, horizonY),
        strokeWidth = (2.5f * approachScale).dp.toPx()
    )

    // Beacon ground ripple circle
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(SandstoneGold.copy(alpha = 0.6f), Color.Transparent),
            center = Offset(pinX, horizonY),
            radius = 24.dp.toPx() * approachScale
        ),
        radius = 24.dp.toPx() * approachScale,
        center = Offset(pinX, horizonY)
    )

    // Floating Badge Card
    val badgeW = (130.dp.toPx() * approachScale)
    val badgeH = (38.dp.toPx() * approachScale)
    val badgeRect = androidx.compose.ui.geometry.RoundRect(
        pinX - badgeW / 2f,
        pinY - badgeH,
        pinX + badgeW / 2f,
        pinY,
        CornerRadius(10.dp.toPx() * approachScale, 10.dp.toPx() * approachScale)
    )

    // Card background
    drawRoundRect(
        color = Color(0xFF0D1B2A).copy(alpha = 0.95f),
        topLeft = Offset(pinX - badgeW / 2f, pinY - badgeH),
        size = Size(badgeW, badgeH),
        cornerRadius = CornerRadius(10.dp.toPx() * approachScale, 10.dp.toPx() * approachScale)
    )
    drawRoundRect(
        color = SandstoneGold,
        topLeft = Offset(pinX - badgeW / 2f, pinY - badgeH),
        size = Size(badgeW, badgeH),
        cornerRadius = CornerRadius(10.dp.toPx() * approachScale, 10.dp.toPx() * approachScale),
        style = Stroke(width = 1.5.dp.toPx() * approachScale)
    )

    // Step Number Badge (Gold Circle with number)
    val numCircleR = 11.dp.toPx() * approachScale
    val numCircleCx = pinX - badgeW / 2f + 16.dp.toPx() * approachScale
    val numCircleCy = pinY - badgeH / 2f
    drawCircle(color = SandstoneGold, radius = numCircleR, center = Offset(numCircleCx, numCircleCy))

    // Step text
    val stepTextResult = textMeasurer.measure(
        text = activeStep.stepIndex.toString(),
        style = TextStyle(
            color = Color.Black,
            fontSize = (12 * approachScale).sp,
            fontWeight = FontWeight.ExtraBold
        )
    )
    drawText(
        textLayoutResult = stepTextResult,
        topLeft = Offset(
            numCircleCx - stepTextResult.size.width / 2f,
            numCircleCy - stepTextResult.size.height / 2f
        )
    )

    // Site Title
    val title = activeStep.site.getLocalizedTitle(langCode)
    val titleTextResult = textMeasurer.measure(
        text = title,
        style = TextStyle(
            color = Color.White,
            fontSize = (10.5 * approachScale).sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        textLayoutResult = titleTextResult,
        topLeft = Offset(
            numCircleCx + numCircleR + 6.dp.toPx() * approachScale,
            pinY - badgeH / 2f - titleTextResult.size.height / 2f
        )
    )
}
