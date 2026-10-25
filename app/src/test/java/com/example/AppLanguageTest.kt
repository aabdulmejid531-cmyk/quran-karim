package com.example

import com.example.ui.screens.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun missingPreferenceDefaultsToAmharic() {
        assertEquals(AppLanguage.AMHARIC, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.AMHARIC, AppLanguage.fromTag("unsupported"))
    }

    @Test
    fun readerAndSettingsLabelsUseSelectedLanguage() {
        assertEquals("ቅንብሮች", AppLanguage.AMHARIC.text("settings"))
        assertEquals("الإعدادات", AppLanguage.ARABIC.text("settings"))
        assertEquals("Settings", AppLanguage.ENGLISH.text("settings"))
    }

    @Test
    fun appLanguageSelectsMatchingQuranTranslationWhenAvailable() {
        assertEquals("amharic", AppLanguage.AMHARIC.quranTranslationLanguage)
        assertEquals("english", AppLanguage.ENGLISH.quranTranslationLanguage)
        assertEquals("english", AppLanguage.ARABIC.quranTranslationLanguage)
    }

    @Test
    fun readingModesAndExitConfirmationAreLocalized() {
        assertEquals("Quran Text Only", AppLanguage.ENGLISH.startupCopy.quranOnlyTitle)
        assertEquals("Quran with Tafsir", AppLanguage.ENGLISH.startupCopy.quranMeaningTitle)
        assertEquals("Quran with Audio", AppLanguage.ENGLISH.startupCopy.tafsirTitle)
        assertEquals("Exit Quran Karim?", AppLanguage.ENGLISH.text("exitTitle"))
        assertEquals("أضف هذه الآية إلى العلامات", AppLanguage.ARABIC.text("bookmarkAyah"))
    }

    @Test
    fun formattedLabelsPreserveDynamicValues() {
        assertEquals("Page 4, Juz' 2", AppLanguage.ENGLISH.text("pageJuz", 4, 2))
        assertEquals("الصفحة 4، الجزء 2", AppLanguage.ARABIC.text("pageJuz", 4, 2))
    }
}
