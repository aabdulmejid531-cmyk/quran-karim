package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: String, // format: "surahId_ayahNumber"
    val surahId: Int,
    val ayahNumber: Int,
    val arabic: String,
    val english: String,
    val amharic: String,
    val startMs: Long,
    val endMs: Long
)
