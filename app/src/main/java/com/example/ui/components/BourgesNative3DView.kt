package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

data class Native3DBuilding(
    val name: String,
    val x: Float, // world coordinate
    val z: Float,
    val width: Float,
    val depth: Float,
    val height: Float,
    val wallColor: Color,
    val roofColor: Color
)

@Composable
fun BourgesNative3DView(
    onClose: () -> Unit,
    onBuildingSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableStateOf(45f) } // degrees
    var elevationAngle by remember { mutableStateOf(30f) } // degrees
    var zoomScale by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedBuilding by remember { mutableStateOf<Native3DBuilding?>(null) }

    val buildings = remember {
        listOf(
            Native3DBuilding("Cathédrale Saint-Étienne", 0f, 0f, 50f, 80f, 95f, Color(0xFFD5C3A5), Color(0xFFA8381C)),
            Native3DBuilding("Palais Jacques Cœur", -120f, 90f, 60f, 60f, 55f, Color(0xFFC8B18E), Color(0xFF963219)),
            Native3DBuilding("Hôtel Lallemant", 90f, -100f, 45f, 45f, 45f, Color(0xFFDFD0B8), Color(0xFFA8381C)),
            Native3DBuilding("Tour des Échevins", 50f, -50f, 30f, 30f, 50f, Color(0xFFD5C3A5), Color(0xFF8A2C10)),
            Native3DBuilding("Église Saint-Bonnet", 130f, 120f, 50f, 70f, 60f, Color(0xFFCCBA98), Color(0xFFA8381C)),
            Native3DBuilding("Hôtel Cujas", -80f, -90f, 45f, 45f, 45f, Color(0xFFD5C3A5), Color(0xFF963219)),
            Native3DBuilding("Maisons à pans de bois (Place Gordaine)", -40f, 110f, 40f, 55f, 38f, Color(0xFF704214), Color(0xFFA8381C))
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F1E36))) {
        // Native 3D Isometric Canvas Renderer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        rotationAngle += dragAmount.x * 0.5f
                        elevationAngle = (elevationAngle - dragAmount.y * 0.3f).coerceIn(10f, 75f)
                    }
                }
        ) {
            val centerX = size.width / 2f + panOffset.x
            val centerY = size.height / 2f + panOffset.y + 50f

            val radRot = Math.toRadians(rotationAngle.toDouble())
            val cosRot = cos(radRot).toFloat()
            val sinRot = sin(radRot).toFloat()

            val radEl = Math.toRadians(elevationAngle.toDouble())
            val cosEl = cos(radEl).toFloat()
            val sinEl = sin(radEl).toFloat()

            // Project 3D world coordinate (x, y, z) to 2D screen coordinate
            fun project(x: Float, y: Float, z: Float): Offset {
                // Rotate around Y axis
                val rx = x * cosRot - z * sinRot
                val rz = x * sinRot + z * cosRot

                // Isometric / oblique projection with elevation
                val screenX = centerX + rx * zoomScale
                val screenY = centerY - (y * cosEl - rz * sinEl) * zoomScale
                return Offset(screenX, screenY)
            }

            // Draw medieval ground plane & grid
            val groundPath = Path().apply {
                val p1 = project(-250f, 0f, -250f)
                val p2 = project(250f, 0f, -250f)
                val p3 = project(250f, 0f, 250f)
                val p4 = project(-250f, 0f, 250f)
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                close()
            }
            drawPath(groundPath, Color(0xFF1E293B))

            // Sort buildings by depth (painter's algorithm) based on projected Z
            val sortedBuildings = buildings.sortedBy { b ->
                b.x * sinRot + b.z * cosRot
            }

            // Draw buildings
            sortedBuildings.forEach { b ->
                val hw = b.width / 2f
                val hd = b.depth / 2f
                val h = b.height

                // Base footprint corners (x, y=0, z)
                val bl1 = project(b.x - hw, 0f, b.z - hd)
                val bl2 = project(b.x + hw, 0f, b.z - hd)
                val bl3 = project(b.x + hw, 0f, b.z + hd)
                val bl4 = project(b.x - hw, 0f, b.z + hd)

                // Roof corners (x, y=h, z)
                val tl1 = project(b.x - hw, h, b.z - hd)
                val tl2 = project(b.x + hw, h, b.z - hd)
                val tl3 = project(b.x + hw, h, b.z + hd)
                val tl4 = project(b.x - hw, h, b.z + hd)

                // Roof apex
                val apex = project(b.x, h + h * 0.35f, b.z)

                val isSelected = (selectedBuilding?.name == b.name)
                val baseWallColor = if (isSelected) Color(0xFFFDE047) else b.wallColor

                // Draw walls (approximate faces facing camera)
                // We draw vertical walls
                val wallPath1 = Path().apply {
                    moveTo(bl1.x, bl1.y); lineTo(bl2.x, bl2.y); lineTo(tl2.x, tl2.y); lineTo(tl1.x, tl1.y); close()
                }
                drawPath(wallPath1, baseWallColor.copy(alpha = 0.9f))
                drawPath(wallPath1, Color.Black.copy(alpha = 0.2f), style = Stroke(1.5f))

                val wallPath2 = Path().apply {
                    moveTo(bl2.x, bl2.y); lineTo(bl3.x, bl3.y); lineTo(tl3.x, tl3.y); lineTo(tl2.x, tl2.y); close()
                }
                drawPath(wallPath2, baseWallColor.copy(alpha = 0.75f))
                drawPath(wallPath2, Color.Black.copy(alpha = 0.2f), style = Stroke(1.5f))

                // Draw terracotta roof pyramid
                val roof1 = Path().apply { moveTo(tl1.x, tl1.y); lineTo(tl2.x, tl2.y); lineTo(apex.x, apex.y); close() }
                val roof2 = Path().apply { moveTo(tl2.x, tl2.y); lineTo(tl3.x, tl3.y); lineTo(apex.x, apex.y); close() }
                val roof3 = Path().apply { moveTo(tl3.x, tl3.y); lineTo(tl4.x, tl4.y); lineTo(apex.x, apex.y); close() }
                val roof4 = Path().apply { moveTo(tl4.x, tl4.y); lineTo(tl1.x, tl1.y); lineTo(apex.x, apex.y); close() }

                drawPath(roof1, b.roofColor)
                drawPath(roof2, b.roofColor.copy(alpha = 0.85f))
                drawPath(roof3, b.roofColor.copy(alpha = 0.7f))
                drawPath(roof4, b.roofColor.copy(alpha = 0.9f))

                drawPath(roof1, Color(0xFFFDE047).copy(alpha = 0.5f), style = Stroke(1f))
                drawPath(roof2, Color(0xFFFDE047).copy(alpha = 0.5f), style = Stroke(1f))
            }
        }

        // Top Header Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F1E36).copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .border(1.5.dp, Color(0xFFD4AF37).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Moteur 3D Isométrique Natif",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                        Text(
                            text = selectedBuilding?.name ?: "Cité Médiévale de Bourges",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFD4AF37), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Controls for selecting monuments & tour
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    rotationAngle = 45f
                    elevationAngle = 30f
                    zoomScale = 1.0f
                    panOffset = Offset.Zero
                    selectedBuilding = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
            ) {
                Text("🧭 Réinitialiser", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val next = if (selectedBuilding == null) {
                        buildings.first()
                    } else {
                        val idx = buildings.indexOf(selectedBuilding)
                        buildings[(idx + 1) % buildings.size]
                    }
                    selectedBuilding = next
                    onBuildingSelected(next.name)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
            ) {
                Text("🏛️ Visite Guidée", fontWeight = FontWeight.Bold)
            }
        }
    }
}
