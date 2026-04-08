package com.nexus.core.storage.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String = value.orEmpty().joinToString("||")

    @TypeConverter
    fun toStringList(value: String?): List<String> = value
        ?.takeIf { it.isNotBlank() }
        ?.split("||")
        ?: emptyList()
}
