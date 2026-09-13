package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "purchases")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val routeId: Int,
    val routeName: String,
    val amount: Double = 9.0,
    val stripePaymentIntentId: String = "",
    val status: String = "SUCCEEDED",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
