package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases WHERE LOWER(userEmail) = LOWER(:email) ORDER BY timestamp DESC")
    fun getPurchasesForUser(email: String): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseOrder): Long

    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun getPurchaseCount(): Int

    @Query("SELECT SUM(amount) FROM purchases WHERE status = 'SUCCEEDED'")
    suspend fun getTotalRevenue(): Double?

    @Query("SELECT EXISTS(SELECT 1 FROM purchases WHERE LOWER(userEmail) = LOWER(:email) AND routeId = :routeId AND status = 'SUCCEEDED')")
    suspend fun hasUserPurchasedRoute(email: String, routeId: Int): Boolean
}
