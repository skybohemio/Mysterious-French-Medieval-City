package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY isPreset DESC, id ASC")
    fun getAllSitesFlow(): Flow<List<Site>>

    @Query("SELECT COUNT(*) FROM sites")
    suspend fun getSiteCount(): Int

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getSiteById(id: Int): Site?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: Site): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSites(sites: List<Site>): List<Long>

    @Delete
    suspend fun deleteSite(site: Site)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteSiteById(id: Int)
}
