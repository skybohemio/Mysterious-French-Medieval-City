package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

/**
 * A container that displays a single, non-triptych Bourges illustration behind its content,
 * covered with an active, multi-stop dark gradient scrim so that foreground text
 * and controls have 100% contrast, legibility, and visual polish.
 *
 * Uses `Modifier.matchParentSize()` so both the image and the protective dark gradient
 * dynamically and precisely match the exact format and height of the container ("du trou laissé").
 */
@Composable
fun IllustratedScrimBox(
    imageModel: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    topScrimAlpha: Float = 0.55f,
    middleScrimAlpha: Float = 0.75f,
    bottomScrimAlpha: Float = 0.95f,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(Color(0xFF0D1B2A)),
        contentAlignment = contentAlignment
    ) {
        // 1. Illustrated Artwork Layer: single image cropped to fill the exact slot format
        when (imageModel) {
            is Int -> {
                Image(
                    painter = painterResource(id = imageModel),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            is String -> {
                if (imageModel.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .error(R.drawable.img_cathedrale)
                            .fallback(R.drawable.img_cathedrale)
                            .build(),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_cathedrale),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.img_cathedrale),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        // 2. Active Multi-stop Dark Gradient Scrim Layer for pristine text legibility
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = topScrimAlpha),
                            Color.Black.copy(alpha = middleScrimAlpha),
                            Color.Black.copy(alpha = bottomScrimAlpha)
                        )
                    )
                )
        )

        // 3. Crisp Foreground Content with guaranteed contrast
        content()
    }
}
