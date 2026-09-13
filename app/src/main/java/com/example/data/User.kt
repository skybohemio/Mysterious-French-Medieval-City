package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
