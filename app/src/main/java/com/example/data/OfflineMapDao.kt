package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineMapDao {
    @Query("SELECT * FROM offline_map_tiles ORDER BY tileName ASC")
    fun getAllCachedMapTilesFlow(): Flow<List<OfflineMapData>>

    @Query("SELECT COUNT(*) FROM offline_map_tiles")
    suspend fun getTileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTile(tile: OfflineMapData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiles(tiles: List<OfflineMapData>)

    @Query("DELETE FROM offline_map_tiles")
    suspend fun clearAllMapCache()
}
