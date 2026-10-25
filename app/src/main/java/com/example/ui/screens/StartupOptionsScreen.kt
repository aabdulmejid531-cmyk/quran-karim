package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

private const val CREATOR_CONTACT_PHONE = "+251911264314"

@Composable
fun StartupOptionsScreen(
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    var selectedOption by rememberSaveable { mutableStateOf("quran_only") }
    val copy = language.startupCopy

    CompositionLocalProvider(
        LocalLayoutDirection provides language.layoutDirection
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                LanguagePicker(
                    language = language,
                    label = copy.languageLabel,
                    onLanguageSelected = onLanguageSelected
                )

                // Banner image at top of the popup screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_quran_banner),
                        contentDescription = copy.bannerDescription,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient overlay over banner image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )

                    // Banner Text
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Surface(
                            color = Color(0xFFD4AF37).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "القرآن الكريم",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = copy.welcome,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = copy.chooseExperience,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = copy.experienceDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option 1: Arabic Quran text
                    StartupOptionCard(
                        title = copy.quranOnlyTitle,
                        subtitle = copy.quranOnlySubtitle,
                        badge = copy.fullPageBadge,
                        icon = Icons.Default.AutoStories,
                        isSelected = selectedOption == "quran_only",
                        onClick = { selectedOption = "quran_only" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: translation with verse-by-verse tafsir access
                    StartupOptionCard(
                        title = copy.quranMeaningTitle,
                        subtitle = copy.quranMeaningSubtitle,
                        badge = copy.translationsBadge,
                        icon = Icons.Default.Language,
                        isSelected = selectedOption == "quran_tafsir",
                        onClick = { selectedOption = "quran_tafsir" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 3: recitation and audio-synced verse highlighting
                    StartupOptionCard(
                        title = copy.tafsirTitle,
                        subtitle = copy.tafsirSubtitle,
                        badge = copy.tafsirBadge,
                        icon = Icons.Default.PlayArrow,
                        isSelected = selectedOption == "quran_audio",
                        onClick = { selectedOption = "quran_audio" }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Button(
                        onClick = { onOptionSelected(selectedOption) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = copy.continueLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = copy.continueLabel,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { onOptionSelected("quran_only") }
                    ) {
                        Text(
                            text = copy.skipLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Developer Credit & Support Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = copy.supportDescription,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = copy.creatorCredit,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            CreatorContactLink(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguagePicker(
    language: AppLanguage,
    label: String,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Language, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(language.displayName, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onLanguageSelected(option)
                        expanded = false
                    },
                    trailingIcon = {
                        if (option == language) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.AMHARIC }

@Composable
fun appText(key: String, vararg arguments: Any): String =
    LocalAppLanguage.current.text(key, *arguments)

@Composable
fun CreatorContactLink(color: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Text(
        text = appText("contactPhone", CREATOR_CONTACT_PHONE),
        modifier = modifier.clickable {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$CREATOR_CONTACT_PHONE")))
        },
        style = MaterialTheme.typography.labelSmall,
        color = color,
        textDecoration = TextDecoration.Underline
    )
}

enum class AppLanguage(val tag: String, val displayName: String) {
                    AMHARIC("am", "አማርኛ"),
                    ENGLISH("en", "English"),
                    ARABIC("ar", "العربية");

                    val quranTranslationLanguage: String
                        get() = when (this) {
                            AMHARIC -> "amharic"
                            ENGLISH -> "english"
                            ARABIC -> "english"
                        }

                    val layoutDirection: LayoutDirection
                        get() = if (this == ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

                    val startupCopy: StartupCopy
                        get() = when (this) {
                            AMHARIC -> StartupCopy(
                                "የመተግበሪያ ቋንቋ", "የቁርአን ምስል", "እንኳን ወደ ቁርአን ከሪም በደህና መጡ",
                                "የንባብ አማራጭዎን ይምረጡ", "ቅዱስ ቁርአንን እንዴት ማንበብ እንደሚፈልጉ ይምረጡ",
                                "ቁርአን ጽሑፍ ብቻ", "ለአረብኛው የቁርአን ጽሑፍ በሙሉ ገጽ ንባብ።",
                                "አረብኛ ጽሑፍ", "ቁርአን ከተፍሲር ጋር", "የአማርኛ ትርጉም ያንብቡና ለእያንዳንዱ አንቀጽ ተፍሲር ይክፈቱ።",
                                "ትርጉምና ተፍሲር", "የቁርአን ድምጽ", "ቃሪ ይምረጡ፤ አንቀጽን በመንካት ከዚያ አንቀጽ ማዳመጥ ይጀምሩ።",
                                "ድምጽ", "ወደ ቁርአን ይቀጥሉ", "ይዝለሉና ማንበብ ይጀምሩ", "ድጋፍ",
                                "በAbdulkerim Abdulmejid የተዘጋጀ"
                            )
                            ENGLISH -> StartupCopy(
                                "App language", "Quran banner image", "Welcome to Quran Karim",
                                "Choose Your Experience", "Select how you would like to explore the Holy Quran",
                                "Quran Text Only", "Read the Arabic Quran in a clean, full-page layout.",
                                "Arabic text", "Quran with Tafsir", "Read the English translation and open tafsir for any verse.",
                                "Translation & tafsir", "Quran with Audio", "Choose a reciter and tap any verse to listen from that verse.",
                                "Listen by verse", "Continue to Quran", "Skip & Start Reading", "Support",
                                "Created by Abdulkerim Abdulmejid"
                            )
                            ARABIC -> StartupCopy(
                                "لغة التطبيق", "صورة القرآن الكريم", "مرحبًا بكم في القرآن الكريم",
                                "اختر طريقة القراءة", "اختر كيف تريد استكشاف القرآن الكريم",
                                "نص القرآن فقط", "اقرأ نص القرآن العربي في صفحة كاملة.",
                                "النص العربي", "القرآن مع التفسير", "اقرأ الترجمة الإنجليزية وافتح التفسير لأي آية.",
                                "الترجمة والتفسير", "القرآن مع الصوت", "اختر قارئًا والمس أي آية للاستماع بدءًا منها.",
                                "استمع آيةً آية", "متابعة إلى القرآن", "تخطَّ وابدأ القراءة", "الدعم",
                                "إعداد Abdulkerim Abdulmejid"
                            )
                        }

    fun text(key: String, vararg arguments: Any): String {
        val value = when (this) {
            AMHARIC -> amharicText[key]
            ENGLISH -> englishText[key]
            ARABIC -> arabicText[key]
        } ?: englishText[key] ?: key
        return if (arguments.isEmpty()) value else String.format(java.util.Locale.ROOT, value, *arguments)
    }

                    companion object {
                        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: AMHARIC
                    }

                    private val englishText = mapOf(
                        "language" to "App language",
                        "reader" to "Reader", "more" to "More", "settings" to "Settings",
                        "startup" to "Startup Options", "search" to "Search", "searchSurah" to "Search Surah...",
                        "surahs" to "SURAHS", "juz" to "JUZ'", "bookmarks" to "BOOKMARKS",
                        "verses" to "%s - %d verses", "startsAtSurah" to "Starts at Surah %s",
                        "noBookmarks" to "No bookmarked Surahs yet.", "pageJuz" to "Page %d, Juz' %d",
                        "backDirectory" to "Back out to Directory", "bookmark" to "Bookmark",
                        "bookmarkAyah" to "Bookmark this verse", "ayahBookmarkRow" to "Ayah %d",
                        "exitTitle" to "Exit Quran Karim?", "exitMessage" to "Would you like to close the app?",
                        "exit" to "Exit", "cancel" to "Cancel",
                        "languageIcon" to "Language", "menu" to "Menu", "readerSettings" to "Settings & Audio Customization",
                        "readingLayout" to "Reading Mode Layout", "slowScrollStart" to "Start slow auto-scroll",
                        "slowScrollStop" to "Stop slow auto-scroll", "copyAyah" to "Copied!",
                        "previous" to "Previous", "playPause" to "Play/Pause", "stop" to "Stop", "next" to "Next",
                        "reciterSettings" to "Reciter Settings", "qari" to "Qari: %s", "tafsiir" to "Tafsir",
                        "quranSettings" to "Quran Settings & Theme", "colorTheme" to "App Color Theme",
                        "darkEmerald" to "Dark Emerald 💚", "pureBlack" to "Pure Black 🖤", "lightMode" to "Light Mode 🤍",
                        "fontSizes" to "Reading Font Sizes", "quranArabic" to "Quran Arabic", "translation" to "Translation",
                        "selectReciter" to "Select Qari / Reciter", "audioUploads" to "Surah Audio Uploads",
                        "selectSurah" to "Select a Surah", "customAudioSet" to "Custom audio is set for %s.",
                        "noCustomAudio" to "No custom audio selected for this Surah.", "chooseAudio" to "Choose audio file",
                        "removeAudio" to "Remove custom audio", "readingMode" to "Reading Mode",
                        "fullPageQuran" to "Full Page Quran", "stacked" to "Translation", "parallel" to "Parallel",
                        "audio" to "Audio",
                        "closeApply" to "Close & Apply", "authenticTafsir" to "Authentic Tafsir", "close" to "Close",
                        "ibnKathir" to "Ibn Kathir", "asSaadi" to "As-Sa'di",
                        "saveTafsirOffline" to "Save this tafsir for offline reading",
                        "tafsirSavedOffline" to "Tafsir saved for offline reading",
                        "audioSaved" to "Audio saved for this Surah",
                        "audioPermission" to "Could not keep access to this audio file",
                        "audioOpenFailed" to "This audio file could not be opened",
                        "tafsirAudio" to "Settings & Audio Customization", "startupScreen" to "Startup Options Screen",
                        "surah" to "Surah %s", "pageNumber" to "Page %d", "juzNumber" to "Juz' %d",
                        "creatorCredit" to "Created by Abdulkerim Abdulmejid",
                        "contactPhone" to "Contact: %s",
                        "meccan" to "Meccan", "medinan" to "Medinan", "copy" to "Copy",
                        "offlineText" to "Offline Quran text: %d/%d surahs",
                        "internetTextDownload" to "Quran text downloads automatically over Wi-Fi or mobile data.",
                        "offlineAudio" to "Yasser Al-Dosari audio: %d/%d surahs",
                        "wifiAudioDownload" to "Recitations are only downloaded after you choose to download them over Wi-Fi.",
                        "downloadAudio" to "Download Yasser Al-Dosari audio over Wi-Fi",
                        "resumeAudioDownloads" to "Resume Yasser Al-Dosari downloads",
                        "retryTextDownloads" to "Retry Quran text downloads",
                        "waitingInternet" to "Waiting for an internet connection",
                        "waitingWifi" to "Waiting for Wi-Fi",
                        "downloadFailures" to "%d downloads need another attempt",
                        "retryDownloads" to "Retry offline downloads"
                    )

                    private val amharicText = mapOf(
                        "language" to "የመተግበሪያ ቋንቋ",
                        "reader" to "አንባቢ", "more" to "ተጨማሪ", "settings" to "ቅንብሮች",
                        "startup" to "የመጀመሪያ አማራጮች", "search" to "ፈልግ", "searchSurah" to "ሱራን ፈልግ...",
                        "surahs" to "ሱራዎች", "juz" to "ጁዝ", "bookmarks" to "ዕልባቶች",
                        "verses" to "%s - %d አንቀጾች", "startsAtSurah" to "ከሱራ %s ይጀምራል",
                        "noBookmarks" to "እስካሁን ዕልባት የተደረገባቸው ሱራዎች የሉም።", "pageJuz" to "ገጽ %d፣ ጁዝ %d",
                        "backDirectory" to "ወደ ዝርዝሩ ተመለስ", "bookmark" to "ዕልባት",
                        "bookmarkAyah" to "ይህን አንቀጽ ዕልባት አድርግ", "ayahBookmarkRow" to "አንቀጽ %d",
                        "exitTitle" to "ከቁርአን ከሪም ይውጡ?", "exitMessage" to "መተግበሪያውን መዝጋት ይፈልጋሉ?",
                        "exit" to "ውጣ", "cancel" to "ተወው",
                        "languageIcon" to "ቋንቋ", "menu" to "ምናሌ", "readerSettings" to "ቅንብሮችና የድምጽ ማስተካከያ",
                        "readingLayout" to "የንባብ አቀማመጥ", "slowScrollStart" to "ቀስ ብሎ ራስ-ሰር ማሸብለል ጀምር",
                        "slowScrollStop" to "ቀስ ብሎ ራስ-ሰር ማሸብለል አቁም", "copyAyah" to "ተቀድቷል!",
                        "previous" to "ቀዳሚ", "playPause" to "አጫውት/አቁም", "stop" to "አቁም", "next" to "ቀጣይ",
                        "reciterSettings" to "የቃሪ ቅንብሮች", "qari" to "ቃሪ፦ %s", "tafsiir" to "ተፍሲር",
                        "quranSettings" to "የቁርአን ቅንብሮችና ገጽታ", "colorTheme" to "የመተግበሪያ ቀለም",
                        "darkEmerald" to "ጥቁር ኤመራልድ 💚", "pureBlack" to "ጥቁር 🖤", "lightMode" to "ብርሃን 🤍",
                        "fontSizes" to "የንባብ ፊደል መጠኖች", "quranArabic" to "የቁርአን አረብኛ", "translation" to "ትርጉም",
                        "selectReciter" to "ቃሪ / አንባቢ ይምረጡ", "audioUploads" to "የሱራ ድምጽ ፋይሎች",
                        "selectSurah" to "ሱራ ይምረጡ", "customAudioSet" to "ብጁ ድምጽ ለ%s ተዘጋጅቷል።",
                        "noCustomAudio" to "ለዚህ ሱራ ብጁ ድምጽ አልተመረጠም።", "chooseAudio" to "የድምጽ ፋይል ይምረጡ",
                        "removeAudio" to "ብጁ ድምጹን አስወግድ", "readingMode" to "የንባብ ሁነታ",
                        "fullPageQuran" to "ሙሉ ገጽ ቁርአን", "stacked" to "ትርጉም", "parallel" to "ጎን ለጎን",
                        "audio" to "ድምጽ",
                        "closeApply" to "ዝጋና ተግብር", "authenticTafsir" to "ትክክለኛ ተፍሲር", "close" to "ዝጋ",
                        "ibnKathir" to "ኢብኑ ከሲር", "asSaadi" to "አስ-ሰዕዲ",
                        "saveTafsirOffline" to "ይህን ተፍሲር ከመስመር ውጭ ለማንበብ አስቀምጥ",
                        "tafsirSavedOffline" to "ተፍሲሩ ከመስመር ውጭ ለማንበብ ተቀምጧል",
                        "audioSaved" to "የዚህ ሱራ ድምጽ ተቀምጧል",
                        "audioPermission" to "የዚህን የድምጽ ፋይል ፈቃድ ማስቀጠል አልተቻለም",
                        "audioOpenFailed" to "ይህን የድምጽ ፋይል መክፈት አልተቻለም",
                        "startupScreen" to "የመጀመሪያ አማራጮች", "surah" to "ሱራ %s",
                        "pageNumber" to "ገጽ %d", "juzNumber" to "ጁዝ %d",
                        "meccan" to "መካዊ", "medinan" to "መዲናዊ",
                        "creatorCredit" to "በAbdulkerim Abdulmejid የተዘጋጀ",
                        "contactPhone" to "ያግኙን፦ %s",
                        "copy" to "ቅዳ",
                        "offlineText" to "ከመስመር ውጭ የቁርአን ጽሑፍ፦ %d/%d ሱራዎች",
                        "internetTextDownload" to "የቁርአን ጽሑፍ በዋይ-ፋይ ወይም በሞባይል ዳታ በራስ-ሰር ይወርዳል።",
                        "offlineAudio" to "የያስር አል-ዶሳሪ ድምጽ፦ %d/%d ሱራዎች",
                        "wifiAudioDownload" to "ድምጽ ማውረድ በዋይ-ፋይ ላይ በእርስዎ ምርጫ ብቻ ይጀምራል።",
                        "downloadAudio" to "የያስር አል-ዶሳሪ ድምጽን በዋይ-ፋይ አውርድ",
                        "resumeAudioDownloads" to "የያስር አል-ዶሳሪ ማውረድን ቀጥል",
                        "retryTextDownloads" to "የቁርአን ጽሑፍ ማውረድን እንደገና ሞክር",
                        "waitingInternet" to "የኢንተርኔት ግንኙነት በመጠባበቅ ላይ",
                        "waitingWifi" to "ዋይ-ፋይ በመጠባበቅ ላይ",
                        "downloadFailures" to "%d ማውረዶች እንደገና መሞከር ያስፈልጋቸዋል",
                        "retryDownloads" to "ከመስመር ውጭ ማውረዶችን እንደገና ሞክር"
                    )

                    private val arabicText = mapOf(
                        "language" to "لغة التطبيق",
                        "reader" to "القارئ", "more" to "المزيد", "settings" to "الإعدادات",
                        "startup" to "خيارات البداية", "search" to "بحث", "searchSurah" to "ابحث عن سورة...",
                        "surahs" to "السور", "juz" to "الأجزاء", "bookmarks" to "العلامات المرجعية",
                        "verses" to "%s - %d آيات", "startsAtSurah" to "يبدأ من سورة %s",
                        "noBookmarks" to "لا توجد سور محفوظة بعد.", "pageJuz" to "الصفحة %d، الجزء %d",
                        "backDirectory" to "العودة إلى الفهرس", "bookmark" to "إضافة إلى العلامات المرجعية",
                        "bookmarkAyah" to "أضف هذه الآية إلى العلامات", "ayahBookmarkRow" to "الآية %d",
                        "exitTitle" to "الخروج من قرآن كريم؟", "exitMessage" to "هل تريد إغلاق التطبيق؟",
                        "exit" to "خروج", "cancel" to "إلغاء",
                        "languageIcon" to "اللغة", "menu" to "القائمة", "readerSettings" to "الإعدادات وتخصيص الصوت",
                        "readingLayout" to "تخطيط القراءة", "slowScrollStart" to "بدء التمرير التلقائي البطيء",
                        "slowScrollStop" to "إيقاف التمرير التلقائي البطيء", "copyAyah" to "تم النسخ!",
                        "previous" to "السابق", "playPause" to "تشغيل/إيقاف مؤقت", "stop" to "إيقاف", "next" to "التالي",
                        "reciterSettings" to "إعدادات القارئ", "qari" to "القارئ: %s", "tafsiir" to "التفسير",
                        "quranSettings" to "إعدادات القرآن والمظهر", "colorTheme" to "ألوان التطبيق",
                        "darkEmerald" to "زمردي داكن 💚", "pureBlack" to "أسود خالص 🖤", "lightMode" to "الوضع الفاتح 🤍",
                        "fontSizes" to "أحجام خط القراءة", "quranArabic" to "القرآن بالعربية", "translation" to "الترجمة",
                        "selectReciter" to "اختر القارئ", "audioUploads" to "ملفات صوت السور",
                        "selectSurah" to "اختر سورة", "customAudioSet" to "تم تعيين صوت مخصص لسورة %s.",
                        "noCustomAudio" to "لم يتم اختيار صوت مخصص لهذه السورة.", "chooseAudio" to "اختر ملفًا صوتيًا",
                        "removeAudio" to "إزالة الصوت المخصص", "readingMode" to "وضع القراءة",
                        "fullPageQuran" to "القرآن صفحة كاملة", "stacked" to "الترجمة", "parallel" to "متوازٍ",
                        "audio" to "الصوت",
                        "closeApply" to "إغلاق وتطبيق", "authenticTafsir" to "التفسير الموثوق", "close" to "إغلاق",
                        "ibnKathir" to "ابن كثير", "asSaadi" to "السعدي",
                        "saveTafsirOffline" to "احفظ هذا التفسير للقراءة دون اتصال",
                        "tafsirSavedOffline" to "تم حفظ التفسير للقراءة دون اتصال",
                        "audioSaved" to "تم حفظ صوت هذه السورة",
                        "audioPermission" to "تعذر الاحتفاظ بإمكانية الوصول إلى هذا الملف الصوتي",
                        "audioOpenFailed" to "تعذر فتح هذا الملف الصوتي",
                        "startupScreen" to "خيارات البداية", "surah" to "سورة %s",
                        "pageNumber" to "الصفحة %d", "juzNumber" to "الجزء %d",
                        "meccan" to "مكي", "medinan" to "مدني",
                        "creatorCredit" to "إعداد Abdulkerim Abdulmejid",
                        "contactPhone" to "للتواصل: %s",
                        "copy" to "نسخ",
                        "offlineText" to "نص القرآن دون اتصال: %d/%d سورة",
                        "internetTextDownload" to "يتم تنزيل نص القرآن تلقائيًا عبر Wi-Fi أو بيانات الهاتف.",
                        "offlineAudio" to "تلاوة ياسر الدوسري: %d/%d سورة",
                        "wifiAudioDownload" to "لا يبدأ تنزيل التلاوات إلا عند اختيارك ذلك عبر Wi-Fi.",
                        "downloadAudio" to "تنزيل تلاوات ياسر الدوسري عبر Wi-Fi",
                        "resumeAudioDownloads" to "استئناف تنزيلات ياسر الدوسري",
                        "retryTextDownloads" to "إعادة تنزيل نص القرآن",
                        "waitingInternet" to "بانتظار اتصال بالإنترنت",
                        "waitingWifi" to "بانتظار شبكة Wi-Fi",
                        "downloadFailures" to "%d تنزيلات تحتاج إلى إعادة المحاولة",
                        "retryDownloads" to "إعادة محاولة التنزيلات"
                    )
                }

data class StartupCopy(
                    val languageLabel: String,
                    val bannerDescription: String,
                    val welcome: String,
                    val chooseExperience: String,
                    val experienceDescription: String,
                    val quranOnlyTitle: String,
                    val quranOnlySubtitle: String,
                    val fullPageBadge: String,
                    val quranMeaningTitle: String,
                    val quranMeaningSubtitle: String,
                    val translationsBadge: String,
                    val tafsirTitle: String,
                    val tafsirSubtitle: String,
                    val tafsirBadge: String,
                    val continueLabel: String,
                    val skipLabel: String,
                    val supportDescription: String,
                    val creatorCredit: String
)

@Composable
private fun StartupOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selection Radio Indicator
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
