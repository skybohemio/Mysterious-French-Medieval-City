package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BourgesThreeJS3DView(
    onClose: () -> Unit,
    onBuildingSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BourgesNative3DView(
        onClose = onClose,
        onBuildingSelected = onBuildingSelected,
        modifier = modifier
    )
}
