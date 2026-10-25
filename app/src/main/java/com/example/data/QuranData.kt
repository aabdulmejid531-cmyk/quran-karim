package com.example.data

data class Ayah(
    val number: Int,
    val arabic: String,
    val english: String,
    val amharic: String,
    val startMs: Long,
    val endMs: Long
)

data class Surah(
    val id: Int,
    val name: String,
    val englishName: String,
    val englishMeaning: String,
    val type: String,
    val ayahs: List<Ayah>,
    val audioUrl: String
)

object QuranRepository {
    val allSurahHeaders: List<SurahHeader> = SurahMetadata.list

    // Official King Fahd Glorious Quran Printing Complex Amharic Translation (የንጉስ ፈሀድ የተከበረ ቁርኣን ማተሚያ ማዕከል - ሸይኽ ሙሐመድዘይን ዘህረዲን)
    val offlinePreloadedSurahs = listOf(
        Surah(
            id = 1,
            name = "الفاتحة",
            englishName = "Al-Fatihah",
            englishMeaning = "The Opening",
            type = "Meccan",
            audioUrl = "https://download.quranicaudio.com/quran/mishari_al_afasy/001.mp3",
            ayahs = listOf(
                Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "በአላህ ስም እጅግ በጣም ሩህሩህ በጣም አዛኝ በሆነው (እጀምራለሁ)::", 0, 5900),
                Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "ምስጋና ሁሉ ለዓለማቱ ጌታ ለአላህ ይገባዋል::", 5900, 11500),
                Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "እጅግ በጣም ሩህሩህ በጣም አዛኝ ለሆነው::", 11500, 15000),
                Ayah(4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "የፍርዱ ቀን ባለቤት ለሆነው (ለአላህ ብቻ ይገባዋል)::", 15000, 18500),
                Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "(ሙስሊሞች ሆይ! ጌታችሁን) አንተን ብቻ እንገዛለን:: ካንተም ብቻ እርዳታን እንለምናለን (በሉ)::", 18500, 24600),
                Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "ቀጥተኛውን መንገድ ምራን::", 24600, 29800),
                Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "የእነዚያን ለእነርሱ በጎ የዋልክላቸውን፤ በእነርሱ ላይ ያልተቆጣህባቸውንና ያልተሳሳቱትንም ሰዎች መንገድ (ምራን በሉ)::", 29800, 43500)
            )
        ),
        Surah(
            id = 112,
            name = "الإخلاص",
            englishName = "Al-Ikhlas",
            englishMeaning = "Sincerity",
            type = "Meccan",
            audioUrl = "https://download.quranicaudio.com/quran/mishari_al_afasy/112.mp3",
            ayahs = listOf(
                Ayah(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "(መልዕክተኛችን ሙሐመድ ሆይ! እንዲህ) በል፡ -እርሱ አላህ አንድ ብቻ ነው::", 0, 4000),
                Ayah(2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "አላህ የሁሉ ነገር መጠጊያ ነው::", 4000, 7500),
                Ayah(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "አልወለደም፤ አልተወለደምም::", 7500, 12000),
                Ayah(4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", "ለእርሱም አንድም ብጤ የለዉም።", 12000, 21500)
            )
        ),
        Surah(
            id = 113,
            name = "الفلق",
            englishName = "Al-Falaq",
            englishMeaning = "The Daybreak",
            type = "Meccan",
            audioUrl = "https://download.quranicaudio.com/quran/mishari_al_afasy/113.mp3",
            ayahs = listOf(
                Ayah(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "(መልዕክተኛችን ሙሐመድ ሆይ! እንዲህ) በል፡ -በተፈልቃቂው ጎህ (በአል-ፈለቅ) ጌታ እጠበቃለሁ፤-", 0, 4500),
                Ayah(2, "مِنْ شَرِّ مَا خَلَقَ", "From the evil of that which He created", "ከፈጠረው ፍጡር ሁሉ ክፋት::", 4500, 8000),
                Ayah(3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "ከሌሊትም ክፋት በጨለመ ጊዜ፤", 8000, 13000),
                Ayah(4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "በቋጠሮዎች ላይ ተፊዎች ከሆኑት ደጋሚ ሴቶችም ክፋት፤", 13000, 19000),
                Ayah(5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "ከምቀኛም ክፋት፤ በተመቀኘ ጊዜ (በአሏህ) -እጠበቃለሁ- በል::", 19000, 25500)
            )
        ),
        Surah(
            id = 114,
            name = "الناس",
            englishName = "An-Nas",
            englishMeaning = "The Mankind",
            type = "Meccan",
            audioUrl = "https://download.quranicaudio.com/quran/mishari_al_afasy/114.mp3",
            ayahs = listOf(
                Ayah(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "(መልዕክተኛችን ሙሐመድ ሆይ! እንዲህ) በል፡ -የሰዎች ሁሉ ፈጣሪ በሆነው ጌታ እጠበቃለሁ፤", 0, 5000),
                Ayah(2, "مَلِكِ النَّاسِ", "The Sovereign of mankind.", "የሰዎች ሁሉ ንጉስ በሆነው ጌታ፤", 5000, 8000),
                Ayah(3, "إِلَٰهِ النَّاسِ", "The God of mankind,", "የሰዎች ሁሉ አምላክ በሆነው ጌታ፤", 8000, 11000),
                Ayah(4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "ከዚያ ብቅ እልም ባይ ከሆነው ጎትጓች ክፋት፤", 11000, 17000),
                Ayah(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "ከዚያ በሰዎች ልቦች ውስጥ ከሚጎተጉተው፤", 17000, 23500),
                Ayah(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "ከጂንም ይሁን ከሰው ሰይጣን ክፋት (እጠበቃለሁ)።", 23500, 31500)
            )
        )
    )
}
