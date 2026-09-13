package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id ASC")
    fun getAllRoutesFlow(): Flow<List<TourRoute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: TourRoute): Long

    @Delete
    suspend fun deleteRoute(route: TourRoute)

    @Update
    suspend fun updateRoute(route: TourRoute)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRouteById(id: Int)

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: Int): TourRoute?
}
