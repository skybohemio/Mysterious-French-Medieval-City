package com.example.ui.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

data class OsmBuilding3D(
    val name: String,
    val heightMeters: Float,
    val latLngs: List<Pair<Double, Double>>
)

class Bourges3DBuildingOverlay(
    private val buildings: List<OsmBuilding3D>,
    private val isEnabled: Boolean
) : Overlay() {

    private val wallPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val roofPaint = Paint().apply {
        color = Color.parseColor("#A8381C") // Authentic French terracotta tile roof
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val roofOutlinePaint = Paint().apply {
        color = Color.parseColor("#E6C687") // Soft warm golden outline
        style = Paint.Style.STROKE
        strokeWidth = 2.0f
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (!isEnabled || shadow) return
        val projection = mapView.projection
        val zoom = mapView.zoomLevelDouble

        // Scale factor proportional to osmdroid map zoom level for medieval French town scale
        val scaleFactor = (zoom - 10.0).coerceAtLeast(0.8).toFloat() * 0.9f

        buildings.forEach { bldg ->
            // Project geographical coordinates (GeoPoint) to 2D screen pixels ensuring tile alignment
            val basePoints = bldg.latLngs.map { gp ->
                val screenPt = projection.toPixels(GeoPoint(gp.first, gp.second), null)
                PointF(screenPt.x.toFloat(), screenPt.y.toFloat())
            }
            if (basePoints.size < 3) return@forEach

            val heightPx = bldg.heightMeters * scaleFactor

            // Isometric 3D projection shift towards top-left
            val roofPoints = basePoints.map { pt ->
                PointF(pt.x - (heightPx * 0.22f), pt.y - heightPx)
            }

            // Draw vertical medieval walls connecting base footprint to roof
            for (i in basePoints.indices) {
                val next = (i + 1) % basePoints.size
                val wallPath = Path().apply {
                    moveTo(basePoints[i].x, basePoints[i].y)
                    lineTo(basePoints[next].x, basePoints[next].y)
                    lineTo(roofPoints[next].x, roofPoints[next].y)
                    lineTo(roofPoints[i].x, roofPoints[i].y)
                    close()
                }

                // Authentic medieval limestone and timber framing tone with shading
                wallPaint.color = if (i % 2 == 0) Color.parseColor("#D5C3A5") else Color.parseColor("#B8A381")
                canvas.drawPath(wallPath, wallPaint)
            }

            // Draw terracotta roof polygon
            val roofPath = Path().apply {
                moveTo(roofPoints[0].x, roofPoints[0].y)
                for (i in 1 until roofPoints.size) {
                    lineTo(roofPoints[i].x, roofPoints[i].y)
                }
                close()
            }
            canvas.drawPath(roofPath, roofPaint)
            canvas.drawPath(roofPath, roofOutlinePaint)
        }
    }
}

object Bourges3DBuildingsDataSource {
    fun getBourgesLandmarkBuildings(): List<OsmBuilding3D> {
        return listOf(
            OsmBuilding3D(
                name = "Cathédrale Saint-Étienne",
                heightMeters = 24f,
                latLngs = listOf(
                    47.0824 to 2.3995,
                    47.0828 to 2.4002,
                    47.0820 to 2.4010,
                    47.0816 to 2.4003
                )
            ),
            OsmBuilding3D(
                name = "Palais Jacques Cœur",
                heightMeters = 16f,
                latLngs = listOf(
                    47.0805 to 2.3932,
                    47.0810 to 2.3940,
                    47.0802 to 2.3948,
                    47.0797 to 2.3940
                )
            ),
            OsmBuilding3D(
                name = "Hôtel Lallemant",
                heightMeters = 12f,
                latLngs = listOf(
                    47.0850 to 2.3960,
                    47.0854 to 2.3966,
                    47.0848 to 2.3972,
                    47.0844 to 2.3966
                )
            ),
            OsmBuilding3D(
                name = "Tour des Échevins",
                heightMeters = 14f,
                latLngs = listOf(
                    47.0842 to 2.3958,
                    47.0845 to 2.3962,
                    47.0841 to 2.3966,
                    47.0838 to 2.3962
                )
            ),
            OsmBuilding3D(
                name = "Église Saint-Bonnet",
                heightMeters = 16f,
                latLngs = listOf(
                    47.0872 to 2.4030,
                    47.0876 to 2.4036,
                    47.0870 to 2.4042,
                    47.0866 to 2.4036
                )
            ),
            OsmBuilding3D(
                name = "Hôtel Cujas",
                heightMeters = 12f,
                latLngs = listOf(
                    47.0838 to 2.3968,
                    47.0842 to 2.3974,
                    47.0836 to 2.3980,
                    47.0832 to 2.3974
                )
            ),
            OsmBuilding3D(
                name = "Place Gordaine Maisons à pans de bois",
                heightMeters = 10f,
                latLngs = listOf(
                    47.0858 to 2.3960,
                    47.0862 to 2.3966,
                    47.0856 to 2.3972,
                    47.0852 to 2.3966
                )
            )
        )
    }
}
