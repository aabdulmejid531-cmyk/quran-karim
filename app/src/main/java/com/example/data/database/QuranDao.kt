package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuranDao {
    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY ayahNumber ASC")
    suspend fun getAyahsForSurah(surahId: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT DISTINCT surahId FROM ayahs")
    suspend fun getCachedSurahIds(): List<Int>

    @Query("DELETE FROM ayahs WHERE surahId = :surahId")
    suspend fun deleteAyahsForSurah(surahId: Int)

    @Query("DELETE FROM ayahs")
    suspend fun clearAllAyahs()

    @Query("SELECT * FROM tafsirs WHERE tafsirType = :tafsirType AND surahId = :surahId AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getTafsir(tafsirType: String, surahId: Int, ayahNumber: Int): TafsirEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsir(tafsir: TafsirEntity)
}
