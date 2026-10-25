package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tafsirs")
data class TafsirEntity(
    @PrimaryKey val id: String,
    val surahId: Int,
    val ayahNumber: Int,
    val tafsirType: String,
    val tafsirText: String
)
