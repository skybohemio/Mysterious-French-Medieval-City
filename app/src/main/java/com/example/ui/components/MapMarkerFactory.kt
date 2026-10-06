package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/**
 * Factory for creating modern, fine, needle-precise map markers for OpenStreetMap.
 * Replaces the bulky default green marker with a white pointing hand.
 */
object MapMarkerFactory {

    private val cache = mutableMapOf<String, Drawable>()

    fun getSiteMarker(
        context: Context,
        category: String,
        isSelected: Boolean = false
    ): Drawable {
        val key = "${category.uppercase()}_${if (isSelected) "SEL" else "NORM"}"
        return cache.getOrPut(key) {
            createFinePinDrawable(context, category, isSelected)
        }
    }

    fun getViewPointMarker(context: Context): Drawable {
        val key = "VIEWPOINT_SCENIC"
        return cache.getOrPut(key) {
            createScenicViewPointDrawable(context)
        }
    }

    fun getUserLocationMarker(context: Context): Drawable {
        val key = "USER_LOCATION_GPS"
        return cache.getOrPut(key) {
            createUserGpsDrawable(context)
        }
    }

    /**
     * Numbered Step Marker for an active tour route (1, 2, 3, 4...).
     */
    fun getRouteStepMarker(
        context: Context,
        stepNumber: Int,
        isSelected: Boolean = false
    ): Drawable {
        val key = "ROUTE_STEP_${stepNumber}_${if (isSelected) "SEL" else "NORM"}"
        return cache.getOrPut(key) {
            createStepPinDrawable(context, stepNumber, isSelected)
        }
    }

    /**
     * Subtle small dot for sites not in the active tour.
     */
    fun getDimmedSiteMarker(context: Context): Drawable {
        val key = "SITE_DIMMED"
        return cache.getOrPut(key) {
            createDimmedDotDrawable(context)
        }
    }

    private fun getCategoryColor(category: String): Int {
        return when (category.uppercase()) {
            "CATHEDRAL" -> 0xFFC5A059.toInt()  // Sandstone Gold
            "PALACE" -> 0xFF7B1FA2.toInt()     // Royal Violet
            "NATURE" -> 0xFF2E7D32.toInt()     // Emerald Green
            "MUSEUM" -> 0xFFE65100.toInt()     // Warm Amber
            "ALCHIMIE" -> 0xFF1565C0.toInt()   // Mystic Blue
            "SORCELLERIE" -> 0xFF880E4F.toInt()// Sorcery Crimson
            "SOUTERRAINS" -> 0xFF37474F.toInt()// Subterranean Slate
            "TEMPLIERS" -> 0xFFB71C1C.toInt()  // Templar Red
            else -> 0xFF1A365D.toInt()         // Regal Navy
        }
    }

    /**
     * Builds an ultra-fine, needle-sharp pin:
     * - Slender tapering body ending at exact pixel tip at bottom center.
     * - Crisp white outer contour for high contrast against any street map.
     * - Clean concentric center dot for modern minimalist elegance.
     */
    private fun createFinePinDrawable(
        context: Context,
        category: String,
        isSelected: Boolean
    ): Drawable {
        val density = context.resources.displayMetrics.density
        val baseColor = getCategoryColor(category)

        // Dimensions in dp scaled by density
        val widthDp = if (isSelected) 32f else 26f
        val heightDp = if (isSelected) 44f else 36f

        val w = (widthDp * density).toInt()
        val h = (heightDp * density).toInt()

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = w / 2f
        val topPadding = (if (isSelected) 4f else 2f) * density
        val bottomMargin = 2f * density
        val tipY = h - bottomMargin
        val radius = (if (isSelected) 12f else 10f) * density
        val cy = topPadding + radius

        // Paint setup
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = baseColor
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.WHITE
            strokeWidth = 1.5f * density
        }

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x40000000 // subtle shadow
        }

        // 1. Draw subtle ground contact shadow under tip
        val shadowW = 6f * density
        val shadowH = 2.5f * density
        canvas.drawOval(
            RectF(cx - shadowW, tipY - shadowH / 2f, cx + shadowW, tipY + shadowH / 2f),
            shadowPaint
        )

        // 2. Halo for selected state
        if (isSelected) {
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = 0xFFFFD700.toInt() // Vibrant Gold
                strokeWidth = 2.5f * density
            }
            canvas.drawCircle(cx, cy, radius + 3f * density, haloPaint)
        }

        // 3. Construct needle teardrop path
        val path = Path()
        // Tangent angle from circle to tip
        val angle = 32.0 // degrees from horizontal
        val rad = Math.toRadians(angle)
        val dx = (radius * cos(rad)).toFloat()
        val dy = (radius * sin(rad)).toFloat()

        // Start from right tangent of head
        path.moveTo(cx + dx, cy + dy)
        // Straight line down to the fine needle tip
        path.lineTo(cx, tipY)
        // Straight line up to left tangent of head
        path.lineTo(cx - dx, cy + dy)
        // Circular arc covering the upper head
        val circleRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        val startArcAngle = 90f + angle.toFloat()
        val sweepAngle = 360f - 2f * angle.toFloat()
        path.arcTo(circleRect, startArcAngle, sweepAngle, false)
        path.close()

        // Draw pin body & crisp white outline
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)

        // 4. Draw inner core: white circle with tiny colored pip
        val whiteCoreRadius = radius * 0.48f
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(cx, cy, whiteCoreRadius, whitePaint)

        val innerPipRadius = whiteCoreRadius * 0.55f
        val pipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isSelected) 0xFFFF9800.toInt() else baseColor
        }
        canvas.drawCircle(cx, cy, innerPipRadius, pipPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Scenic Viewpoint marker: an elegant golden disc with a camera symbol.
     */
    private fun createScenicViewPointDrawable(context: Context): Drawable {
        val density = context.resources.displayMetrics.density
        val size = (32f * density).toInt()

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f
        val r = (12f * density)

        // Disc background (Gold)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFFC107.toInt()
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.WHITE
            strokeWidth = 2f * density
        }
        canvas.drawCircle(cx, cy, r, fillPaint)
        canvas.drawCircle(cx, cy, r, strokePaint)

        // Small camera body
        val camPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF1A365D.toInt()
        }
        val camW = 12f * density
        val camH = 8f * density
        canvas.drawRoundRect(
            RectF(cx - camW / 2f, cy - camH / 2f + 1f * density, cx + camW / 2f, cy + camH / 2f + 1f * density),
            2f * density, 2f * density,
            camPaint
        )

        // Lens
        val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(cx, cy + 1f * density, 2.5f * density, lensPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * User GPS Location dot: a pulsing blue beacon with pure white border.
     */
    private fun createUserGpsDrawable(context: Context): Drawable {
        val density = context.resources.displayMetrics.density
        val size = (28f * density).toInt()

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f

        // Outer glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x442196F3 // translucent blue
        }
        canvas.drawCircle(cx, cy, 12f * density, glowPaint)

        // White ring
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(cx, cy, 7f * density, whitePaint)

        // Blue core
        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF1976D2.toInt()
        }
        canvas.drawCircle(cx, cy, 5f * density, bluePaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Needle teardrop marker displaying a bold step number (1, 2, 3...) for tour guidance.
     */
    private fun createStepPinDrawable(
        context: Context,
        stepNumber: Int,
        isSelected: Boolean
    ): Drawable {
        val density = context.resources.displayMetrics.density
        val baseColor = if (isSelected) 0xFFFFB300.toInt() else 0xFF1A365D.toInt() // Vibrant Amber or Deep Regal Blue

        val widthDp = if (isSelected) 36f else 30f
        val heightDp = if (isSelected) 50f else 42f

        val w = (widthDp * density).toInt()
        val h = (heightDp * density).toInt()

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = w / 2f
        val topPadding = (if (isSelected) 5f else 3f) * density
        val bottomMargin = 2f * density
        val tipY = h - bottomMargin
        val radius = (if (isSelected) 14f else 12f) * density
        val cy = topPadding + radius

        // Shadow under tip
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x50000000
        }
        val shadowW = 7f * density
        val shadowH = 3f * density
        canvas.drawOval(
            RectF(cx - shadowW, tipY - shadowH / 2f, cx + shadowW, tipY + shadowH / 2f),
            shadowPaint
        )

        // Pulsing radar / outer halo when selected
        if (isSelected) {
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = 0xFFFFD54F.toInt()
                strokeWidth = 3f * density
            }
            canvas.drawCircle(cx, cy, radius + 4f * density, haloPaint)
        }

        // Teardrop path
        val path = Path()
        val angle = 32.0
        val rad = Math.toRadians(angle)
        val dx = (radius * cos(rad)).toFloat()
        val dy = (radius * sin(rad)).toFloat()

        path.moveTo(cx + dx, cy + dy)
        path.lineTo(cx, tipY)
        path.lineTo(cx - dx, cy + dy)
        val circleRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        val startArcAngle = 90f + angle.toFloat()
        val sweepAngle = 360f - 2f * angle.toFloat()
        path.arcTo(circleRect, startArcAngle, sweepAngle, false)
        path.close()

        // Fill body
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = baseColor
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.WHITE
            strokeWidth = 2f * density
        }
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)

        // Inner white disc for the number
        val discRadius = radius * 0.65f
        val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(cx, cy, discRadius, discPaint)

        // Step number text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            textSize = (if (isSelected) 14f else 12f) * density
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(stepNumber.toString(), cx, textY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Subtle small gray dot for POIs outside the active tour.
     */
    private fun createDimmedDotDrawable(context: Context): Drawable {
        val density = context.resources.displayMetrics.density
        val size = (16f * density).toInt()

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f
        val r = 5f * density

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x669E9E9E // Semi-transparent gray
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0xAAFFFFFF.toInt()
            strokeWidth = 1f * density
        }
        canvas.drawCircle(cx, cy, r, fillPaint)
        canvas.drawCircle(cx, cy, r, strokePaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
