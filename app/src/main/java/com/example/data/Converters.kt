package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.toIntOrNull() }
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }
}
