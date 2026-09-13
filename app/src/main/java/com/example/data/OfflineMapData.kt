package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_map_tiles")
data class OfflineMapData(
    @PrimaryKey
    val tileKey: String, // e.g. "bourges_center", "bourges_marais", "bourges_avaricum", "bourges_auron"
    val tileName: String, // Human-readable zone name
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    val zoomLevel: Int = 15,
    val featureJson: String = "", // Map features / vector styling
    val cachedTimestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 128000L
)
