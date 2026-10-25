package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.app.DownloadManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Ayah
import com.example.data.QuranRepository
import com.example.data.Surah
import com.example.data.SurahHeader
import com.example.data.database.AyahEntity
import com.example.data.database.QuranDatabase
import com.example.data.database.TafsirEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.net.toUri
import java.util.concurrent.atomic.AtomicLong

enum class PlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class TrackGroup {
    ALL_SURAHS,
    MECCAN,
    MEDINAN
}

enum class ReadingLayout {
    PARALLEL, // Split-screen side by side on wide screens, elegant stacked row on mobile
    STACKED,  // Top-down row layout
    ARABIC_ONLY, // Continuous Mushaf full page view
    AUDIO
}

data class AyahBookmark(val surahId: Int, val ayahNumber: Int)

enum class AppTheme {
    EMERALD_GREEN, // Default Dark Emerald Green (#071F1A / #10332B / #10B981) matching user's screenshot!
    DARK_BLACK,    // Pure Dark (#121212 / #1E1E1E)
    LIGHT          // Light Mode (#F4F7F5 / #FFFFFF)
}

private val omarHishamAudioSources = listOf(
    "https://ia600600.us.archive.org/24/items/omar-hisham-al-arabi/" to setOf(
        1, 12, 14, 16, 17, 18, 19, 20, 22, 25, 32, 36, 40, 44, 49, 50, 51, 52, 53, 54,
        55, 56, 58, 59, 61, 62, 66, 67, 68, 69, 70, 71, 72, 73, 75, 76, 77, 78, 79, 80,
        81, 82, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
        102, 103, 104, 105, 106, 107, 109, 110, 111, 112, 113, 114
    ),
    "https://ia601902.us.archive.org/30/items/OmarHishamAl-Arabi_murattel/" to setOf(
        1, 2, 18, 36, 44, 49, 51, 52, 53, 54, 55, 56, 58, 62, 66, 67, 68, 69, 70, 71,
        72, 73, 75, 77, 78, 79, 80, 81, 82, 85, 87, 88, 89, 90, 91, 92, 93, 94, 95,
        101, 102, 104, 107, 109, 110, 111, 112, 113, 114
    ),
)

private val omarHishamAdditionalAudioFiles = mapOf(
    33 to "033-surah-al-ahzab.mp3",
    35 to "035-surah-fatir.mp3",
    63 to "063%20surah_al_munafiqun.mp3"
)

private val afifTajuAdditionalSurahs = setOf(
    1, 2, 12, 18, 19, 30, 31, 36, 44, 48, 49, 52, 55, 58, 59, 67, 68, 84, 91, 93, 97, 112
)

data class Reciter(
    val id: String,
    val name: String,
    val baseUrl: String
)

data class TafsirData(
    val tafsirType: String, // "ibnkathir" or "saadi"
    val tafsirName: String,
    val authorName: String,
    val surahId: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val tafsirText: String
)

data class OfflineDownloadProgress(
    val completed: Int = 0,
    val total: Int = 114,
    val failed: Int = 0,
    val waitingForNetwork: Boolean = false
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "QuranViewModel"
    private val sharedPrefs = application.getSharedPreferences("quranic_reader_prefs", Context.MODE_PRIVATE)
    private val quranDao = QuranDatabase.getDatabase(application).quranDao()
    private val textDownloadMutex = Mutex()
    private val downloadManager =
        application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // App Theme State (Default: EMERALD_GREEN as requested in user's image!)
    private val _appTheme = MutableStateFlow(
        try {
            AppTheme.valueOf(sharedPrefs.getString("app_theme", AppTheme.EMERALD_GREEN.name) ?: AppTheme.EMERALD_GREEN.name)
        } catch (e: Exception) {
            AppTheme.EMERALD_GREEN
        }
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    // Complete Qari Reciters List with verified direct streaming endpoints
    val availableReciters = listOf(
        Reciter("yasser", "Yasser Al-Dosari", "https://server11.mp3quran.net/yasser/"),
        Reciter("sudais", "Abdur-Rahman As-Sudais", "https://server11.mp3quran.net/sds/"),
        Reciter("shuraim", "Saud Al-Shuraim", "https://server7.mp3quran.net/shur/"),
        Reciter("maher", "Maher Al-Muaiqly", "https://server12.mp3quran.net/maher/"),
        Reciter("baleela", "Bandar Baleela", "https://server6.mp3quran.net/balilah/"),
        Reciter("juhany", "Abdullah Awad Al-Juhany", "https://server13.mp3quran.net/jhn/"),
        Reciter("budair", "Salah Al-Budair", "https://server6.mp3quran.net/s_bud/"),
        Reciter("hanif", "Abdul Wadood Hanif", "https://server8.mp3quran.net/wd/"),
        Reciter("alijaber", "Ali Jaber", "https://server11.mp3quran.net/a_jbr/"),
        Reciter("basfar", "Abdullah Basfar", "https://server6.mp3quran.net/bsfr/"),
        Reciter("alafasy", "Mishary Rashid Alafasy", "https://server8.mp3quran.net/afs/"),
        Reciter("subhi", "Islam Sobhi", "https://server14.mp3quran.net/islam/"),
        Reciter("omarhisham", "Omar Hisham Al Arabi", "https://ia600600.us.archive.org/24/items/omar-hisham-al-arabi/"),
        Reciter("misbah_sani", "Dr. Misbah Sani (Ethiopia 🇪🇹)", "https://ia601400.us.archive.org/15/items/002_20260317_202603/"),
        Reciter("afif_taju", "Ustaz Afif Taju (Ethiopia 🇪🇹)", "https://ia601606.us.archive.org/30/items/afiftaj/"),
        Reciter("abdulbasit", "Abdul Basit Abdul Samad", "https://server7.mp3quran.net/basit/"),
        Reciter("minshawi", "Saddiq Al-Minshawi", "https://server10.mp3quran.net/minsh/"),
        Reciter("hussary", "Mahmoud Khalil Al-Hussary", "https://server13.mp3quran.net/hussary/")
    )

    private val _selectedReciter = MutableStateFlow(
        availableReciters.firstOrNull { it.id == sharedPrefs.getString("selected_reciter", "yasser") } ?: availableReciters[0]
    )
    val selectedReciter: StateFlow<Reciter> = _selectedReciter.asStateFlow()

    // Cached Surah IDs state to highlight local availability
    private val _cachedSurahIds = MutableStateFlow<Set<Int>>(setOf(1, 112, 113, 114))
    val cachedSurahIds: StateFlow<Set<Int>> = _cachedSurahIds.asStateFlow()
    private val _textDownloadProgress = MutableStateFlow(OfflineDownloadProgress())
    val textDownloadProgress: StateFlow<OfflineDownloadProgress> = _textDownloadProgress.asStateFlow()
    private val _audioDownloadProgress = MutableStateFlow(OfflineDownloadProgress())
    val audioDownloadProgress: StateFlow<OfflineDownloadProgress> = _audioDownloadProgress.asStateFlow()
    private val _audioDownloadRequested = MutableStateFlow(
        sharedPrefs.getBoolean("yasser_audio_download_requested", false)
    )
    val audioDownloadRequested: StateFlow<Boolean> = _audioDownloadRequested.asStateFlow()

    // Active Track Grouping / Classification
    private val _currentTrackGroup = MutableStateFlow(TrackGroup.ALL_SURAHS)
    val currentTrackGroup: StateFlow<TrackGroup> = _currentTrackGroup.asStateFlow()

    // List of Surah headers in the current filter/playlist
    private val _currentPlaylist = MutableStateFlow<List<SurahHeader>>(QuranRepository.allSurahHeaders)
    val currentPlaylist: StateFlow<List<SurahHeader>> = _currentPlaylist.asStateFlow()

    // Active displayed Surah with its loaded Ayahs
    private val _currentSurah = MutableStateFlow<Surah>(QuranRepository.offlinePreloadedSurahs[0])
    val currentSurah: StateFlow<Surah> = _currentSurah.asStateFlow()

    // Loading State for API fetching
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Active translation language: "english" or "amharic"
    private val _selectedLanguage = MutableStateFlow(sharedPrefs.getString("selected_language", "english") ?: "english")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // UI Configuration / Display Settings
    private val _arabicFontSize = MutableStateFlow(sharedPrefs.getFloat("arabic_font_size", 28f))
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    private val _translationFontSize = MutableStateFlow(sharedPrefs.getFloat("translation_font_size", 16f))
    val translationFontSize: StateFlow<Float> = _translationFontSize.asStateFlow()

    private val _readingLayout = MutableStateFlow(
        ReadingLayout.valueOf(sharedPrefs.getString("reading_layout", ReadingLayout.ARABIC_ONLY.name) ?: ReadingLayout.ARABIC_ONLY.name)
    )
    val readingLayout: StateFlow<ReadingLayout> = _readingLayout.asStateFlow()

    // Search query for verses inside the active Surah
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Audio Playback Engine
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val audioRequestId = AtomicLong()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _playbackDuration = MutableStateFlow(1000L) // Default safe non-zero duration
    val playbackDuration: StateFlow<Long> = _playbackDuration.asStateFlow()

    private val _activeAyahNumber = MutableStateFlow<Int?>(null)
    val activeAyahNumber: StateFlow<Int?> = _activeAyahNumber.asStateFlow()
    private val _bookmarkedAyahs = MutableStateFlow(loadBookmarkedAyahs())
    val bookmarkedAyahs: StateFlow<Set<AyahBookmark>> = _bookmarkedAyahs.asStateFlow()

    // Force Offline Mode Switch
    private val _forceOfflineMode = MutableStateFlow(sharedPrefs.getBoolean("force_offline_mode", false))
    val forceOfflineMode: StateFlow<Boolean> = _forceOfflineMode.asStateFlow()

    // Auto-Scroll Centering preference
    private val _autoScrollCentering = MutableStateFlow(sharedPrefs.getBoolean("auto_scroll_centering", true))
    val autoScrollCentering: StateFlow<Boolean> = _autoScrollCentering.asStateFlow()

    // Map of Surah ID -> Uri string of custom uploaded recitation audio
    private val _customSurahAudioUris = MutableStateFlow<Map<Int, String>>(emptyMap())
    val customSurahAudioUris: StateFlow<Map<Int, String>> = _customSurahAudioUris.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(sharedPrefs.getFloat("playback_speed", 1.0f))
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Authentic Tafsir State (Ibn Kathir & Abdur-Rahman Ibn Nasir As-Sa'di)
    private val _currentTafsir = MutableStateFlow<TafsirData?>(null)
    val currentTafsir: StateFlow<TafsirData?> = _currentTafsir.asStateFlow()

    private val _isTafsirLoading = MutableStateFlow(false)
    val isTafsirLoading: StateFlow<Boolean> = _isTafsirLoading.asStateFlow()
    private val _isCurrentTafsirSaved = MutableStateFlow(false)
    val isCurrentTafsirSaved: StateFlow<Boolean> = _isCurrentTafsirSaved.asStateFlow()

    private val _selectedTafsirType = MutableStateFlow("ibnkathir") // "ibnkathir" or "saadi"
    val selectedTafsirType: StateFlow<String> = _selectedTafsirType.asStateFlow()

    init {
        // Load custom audio file URIs from SharedPreferences
        _customSurahAudioUris.value = loadCustomAudioUris()
        cancelPreviousAutomaticAudioDownloads()
        
        // DownloadManager keeps audio downloads running independently of the app process.
        viewModelScope.launch(Dispatchers.IO) {
            prepopulateOfflineCacheIfNeeded()
            refreshCachedSurahIds()
            downloadMissingQuranText()
        }
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (_audioDownloadRequested.value && hasWifiConnection()) {
                    queueYasserAudioDownloads()
                }
                refreshAudioDownloadProgress()
                if (hasValidatedInternet() &&
                    (_textDownloadProgress.value.waitingForNetwork || _textDownloadProgress.value.failed > 0)
                ) {
                    downloadMissingQuranText()
                }
                delay(15_000)
            }
        }

        // Load the initial default Surah
        loadSurah(1, playImmediately = false)
    }

    private fun cancelPreviousAutomaticAudioDownloads() {
        val networkPolicyVersion = 4
        if (sharedPrefs.getInt("yasser_audio_network_policy_version", 0) >= networkPolicyVersion) return

        QuranRepository.allSurahHeaders.forEach { header ->
            val key = yasserAudioDownloadKey(header.id)
            val downloadId = sharedPrefs.getLong(key, -1L)
            if (downloadId > 0L && !isDownloadComplete(downloadId)) {
                downloadManager.remove(downloadId)
                sharedPrefs.edit().remove(key).apply()
            }
        }
        sharedPrefs.edit().putInt("yasser_audio_network_policy_version", networkPolicyVersion).apply()
    }

    fun setAppTheme(theme: AppTheme) {
        _appTheme.value = theme
        sharedPrefs.edit().putString("app_theme", theme.name).apply()
    }

    fun setSelectedReciter(reciter: Reciter) {
        _selectedReciter.value = reciter
        sharedPrefs.edit().putString("selected_reciter", reciter.id).apply()
        
        val surah = _currentSurah.value
        val newUrl = getAudioUrlCandidates(reciter.id, surah.id).firstOrNull()
            ?: reciter.baseUrl + String.format("%03d.mp3", surah.id)
        _currentSurah.value = surah.copy(audioUrl = newUrl)
        stopPlayback()
        playAudio(newUrl)
    }

    private suspend fun refreshCachedSurahIds() {
        try {
            val ids = withContext(Dispatchers.IO) {
                quranDao.getCachedSurahIds()
            }
            _cachedSurahIds.value = ids.toSet() + setOf(1, 112, 113, 114)
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing cached Surah IDs: ${e.message}")
        }
    }

    private suspend fun prepopulateOfflineCacheIfNeeded() {
        try {
            val count = quranDao.getAyahsForSurah(1)
            if (count.isEmpty()) {
                Log.d(tag, "Pre-populating Room offline cache with core Surahs...")
                QuranRepository.offlinePreloadedSurahs.forEach { surah ->
                    val entities = surah.ayahs.map { ayah ->
                        AyahEntity(
                            id = "${surah.id}_${ayah.number}",
                            surahId = surah.id,
                            ayahNumber = ayah.number,
                            arabic = ayah.arabic,
                            english = ayah.english,
                            amharic = ayah.amharic,
                            startMs = ayah.startMs,
                            endMs = ayah.endMs
                        )
                    }
                    quranDao.insertAyahs(entities)
                }
                Log.d(tag, "Room cache pre-population completed successfully!")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error pre-populating database cache: ${e.message}")
        }
    }

    private fun hasValidatedInternet(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun hasWifiConnection(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private suspend fun downloadMissingQuranText() {
        if (!textDownloadMutex.tryLock()) return
        try {
            if (!hasValidatedInternet()) {
                _textDownloadProgress.value = _textDownloadProgress.value.copy(waitingForNetwork = true)
                return
            }

            var completed = 0
            var failed = 0
            val total = QuranRepository.allSurahHeaders.size
            QuranRepository.allSurahHeaders.forEach { header ->
                if (!hasValidatedInternet()) {
                    _textDownloadProgress.value = OfflineDownloadProgress(
                        completed = completed,
                        total = total,
                        failed = failed,
                        waitingForNetwork = true
                    )
                    return
                }
                try {
                    val cached = quranDao.getAyahsForSurah(header.id)
                    if (cached.size == header.numberOfAyahs && cached.all(::hasCompleteAyahText)) {
                        completed++
                    } else {
                        val entities = fetchSurahFromCloudApi(header.id)
                        require(entities.size == header.numberOfAyahs && entities.all(::hasCompleteAyahText)) {
                            "Incomplete verse data for ${header.englishName}"
                        }
                        quranDao.insertAyahs(entities)
                        completed++
                        refreshCachedSurahIds()
                    }
                } catch (exception: Exception) {
                    failed++
                    Log.e(tag, "Could not cache offline text for ${header.englishName}", exception)
                }
                _textDownloadProgress.value = OfflineDownloadProgress(
                    completed = completed,
                    total = total,
                    failed = failed
                )
            }
        } finally {
            textDownloadMutex.unlock()
        }
    }

    fun retryOfflineDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadMissingQuranText()
        }
    }

    fun downloadYasserAudioOverWifi() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!hasWifiConnection()) {
                _errorMessage.value = "Connect to Wi-Fi to download Yasser Al-Dosari recitations."
                refreshAudioDownloadProgress()
                return@launch
            }
            _audioDownloadRequested.value = true
            sharedPrefs.edit().putBoolean("yasser_audio_download_requested", true).apply()
            queueYasserAudioDownloads(retryFailed = true)
            refreshAudioDownloadProgress()
        }
    }

    private fun hasCompleteAyahText(ayah: AyahEntity): Boolean =
        ayah.arabic.isNotBlank() &&
            ayah.english.isNotBlank() &&
            ayah.amharic.isNotBlank() &&
            !ayah.arabic.startsWith("۞ لَقَدْ تَمَّ تَحْضِيرَ") &&
            !ayah.amharic.startsWith("የንጉሥ ፋህድ ማዕከል")

    private fun queueYasserAudioDownloads(retryFailed: Boolean = false) {
        val context = getApplication<Application>()
        QuranRepository.allSurahHeaders.forEach { header ->
            val key = yasserAudioDownloadKey(header.id)
            val existingId = sharedPrefs.getLong(key, -1L)
            if (existingId > 0L) {
                if (isDownloadActiveOrComplete(existingId) || !retryFailed) return@forEach
                downloadManager.remove(existingId)
            }

            val audioUrl = getAudioUrlCandidates("yasser", header.id).firstOrNull()
                ?: "https://server11.mp3quran.net/yasser/${String.format("%03d.mp3", header.id)}"
            try {
                val request = DownloadManager.Request(audioUrl.toUri())
                    .setTitle("Yasser Al-Dosari — ${header.englishName}")
                    .setDescription("Offline Quran recitation")
                    .setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI
                    )
                    .setAllowedOverRoaming(false)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationInExternalFilesDir(
                        context,
                        Environment.DIRECTORY_MUSIC,
                        "quran/yasser/${String.format("%03d.mp3", header.id)}"
                    )
                val downloadId = downloadManager.enqueue(request)
                sharedPrefs.edit().putLong(key, downloadId).apply()
            } catch (exception: Exception) {
                Log.e(tag, "Could not queue Yasser audio for ${header.englishName}", exception)
            }
        }
    }

    private fun yasserAudioDownloadKey(surahId: Int) = "yasser_audio_download_$surahId"

    private fun isDownloadActiveOrComplete(downloadId: Long): Boolean {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return false
        return cursor.use {
            it.moveToFirst() &&
                it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) in setOf(
                    DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    DownloadManager.STATUS_PAUSED,
                    DownloadManager.STATUS_SUCCESSFUL
                )
        }
    }

    private fun isDownloadComplete(downloadId: Long): Boolean {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return false
        return cursor.use {
            it.moveToFirst() &&
                it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) ==
                DownloadManager.STATUS_SUCCESSFUL
        }
    }

    private fun getDownloadedYasserAudioUri(surahId: Int): Uri? {
        val downloadId = sharedPrefs.getLong(yasserAudioDownloadKey(surahId), -1L)
        if (downloadId <= 0L) return null
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return null
        val isSuccessful = cursor.use {
            it.moveToFirst() &&
                it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL
        }
        return if (isSuccessful) downloadManager.getUriForDownloadedFile(downloadId) else null
    }

    private fun refreshAudioDownloadProgress() {
        var completed = 0
        var failed = 0
        QuranRepository.allSurahHeaders.forEach { header ->
            val downloadId = sharedPrefs.getLong(yasserAudioDownloadKey(header.id), -1L)
            if (downloadId <= 0L) return@forEach
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return@forEach
            cursor.use {
                if (!it.moveToFirst()) return@use
                when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> completed++
                    DownloadManager.STATUS_FAILED -> failed++
                }
            }
        }
        _audioDownloadProgress.value = OfflineDownloadProgress(
            completed = completed,
            failed = failed,
            waitingForNetwork = _audioDownloadRequested.value && !hasWifiConnection()
        )
    }

    private fun loadCustomAudioUris(): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("custom_audio_") && value is String) {
                val surahId = key.substringAfter("custom_audio_").toIntOrNull()
                if (surahId != null) {
                    map[surahId] = value
                }
            }
        }
        return map
    }

    fun setCustomSurahAudio(surahId: Int, uriString: String?) {
        val current = _customSurahAudioUris.value.toMutableMap()
        if (uriString == null) {
            current.remove(surahId)
            sharedPrefs.edit().remove("custom_audio_$surahId").apply()
        } else {
            current[surahId] = uriString
            sharedPrefs.edit().putString("custom_audio_$surahId", uriString).apply()
        }
        _customSurahAudioUris.value = current
        Log.d(tag, "Saved custom local audio for Surah $surahId: $uriString")
        
        if (_currentSurah.value.id == surahId) {
            val wasPlaying = _playbackState.value == PlaybackState.PLAYING
            stopPlayback()
            if (wasPlaying) {
                playAudio(_currentSurah.value.audioUrl)
            }
        }
    }

    fun setSelectedLanguage(lang: String) {
        if (lang == "english" || lang == "amharic") {
            _selectedLanguage.value = lang
            sharedPrefs.edit().putString("selected_language", lang).apply()
        }
    }

    fun setForceOfflineMode(enabled: Boolean) {
        _forceOfflineMode.value = enabled
        sharedPrefs.edit().putBoolean("force_offline_mode", enabled).apply()
        if (enabled && _playbackState.value == PlaybackState.PLAYING) {
            stopPlayback()
            playAudio(_currentSurah.value.audioUrl)
        } else if (!enabled && _playbackState.value == PlaybackState.PLAYING) {
            stopPlayback()
            playAudio(_currentSurah.value.audioUrl)
        }
    }

    fun setAutoScrollCentering(enabled: Boolean) {
        _autoScrollCentering.value = enabled
        sharedPrefs.edit().putBoolean("auto_scroll_centering", enabled).apply()
    }

    fun setTrackGroup(group: TrackGroup) {
        if (_currentTrackGroup.value == group) return
        _currentTrackGroup.value = group
        val filteredHeaders = when (group) {
            TrackGroup.ALL_SURAHS -> QuranRepository.allSurahHeaders
            TrackGroup.MECCAN -> QuranRepository.allSurahHeaders.filter { it.type.equals("Makki", ignoreCase = true) || it.type.equals("Meccan", ignoreCase = true) }
            TrackGroup.MEDINAN -> QuranRepository.allSurahHeaders.filter { it.type.equals("Madani", ignoreCase = true) || it.type.equals("Medinan", ignoreCase = true) }
        }
        _currentPlaylist.value = filteredHeaders
    }

    fun loadSurah(
        surahId: Int,
        playImmediately: Boolean = true,
        initialAyahNumber: Int? = null
    ) {
        stopPlayback()
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            val header = QuranRepository.allSurahHeaders.firstOrNull { it.id == surahId }
            if (header == null) {
                _isLoading.value = false
                _errorMessage.value = "Surah with ID $surahId not found."
                return@launch
            }

            val formattedId = String.format("%03d.mp3", surahId)
            val audioUrlForReciter = "${_selectedReciter.value.baseUrl}$formattedId"

            val placeholderSurah = Surah(
                id = header.id,
                name = header.name,
                englishName = header.englishName,
                englishMeaning = header.englishMeaning,
                type = header.type,
                ayahs = emptyList(),
                audioUrl = audioUrlForReciter
            )
            _currentSurah.value = placeholderSurah
            _activeAyahNumber.value = null
            _playbackPosition.value = 0L
            _playbackDuration.value = 1000L

            try {
                var entities = withContext(Dispatchers.IO) {
                    quranDao.getAyahsForSurah(surahId)
                }
                
                if (entities.size != header.numberOfAyahs || entities.any { !hasCompleteAyahText(it) }) {
                    if (_forceOfflineMode.value) {
                        _errorMessage.value = "This Surah is not fully downloaded yet. Connect to Wi-Fi to cache its text."
                        entities = emptyList()
                    } else {
                        try {
                            _errorMessage.value = "Downloading complete Quran text and translations..."
                            entities = fetchSurahFromCloudApi(surahId)
                            require(entities.size == header.numberOfAyahs && entities.all(::hasCompleteAyahText)) {
                                "The complete Quran text was not available for ${header.englishName}."
                            }
                            withContext(Dispatchers.IO) {
                                quranDao.insertAyahs(entities)
                            }
                            refreshCachedSurahIds()
                            _errorMessage.value = null
                        } catch (downloadEx: Exception) {
                            Log.e(tag, "Failed to download complete text for ${header.englishName}", downloadEx)
                            entities = emptyList()
                            _errorMessage.value =
                                "Could not download the complete Quran text for ${header.englishName}. Check your connection and try again."
                        }
                    }
                }
                
                val surah = mapEntitiesToSurah(header, entities, audioUrlForReciter)
                
                _currentSurah.value = surah
                val initialAyah = surah.ayahs.firstOrNull { it.number == initialAyahNumber }
                _activeAyahNumber.value = initialAyah?.number
                _playbackPosition.value = initialAyah?.startMs ?: 0L
                _playbackDuration.value = surah.ayahs.lastOrNull()?.endMs ?: 1000L
                _isLoading.value = false
                
                if (playImmediately) {
                    playAudio(surah.audioUrl, initialAyah?.startMs ?: 0L)
                } else {
                    _playbackState.value = PlaybackState.IDLE
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading Surah $surahId: ${e.message}")
                _isLoading.value = false
                _errorMessage.value = "Error: ${e.localizedMessage ?: e.message}."
                
                _currentSurah.value = placeholderSurah
                _activeAyahNumber.value = null
                _playbackPosition.value = 0L
                _playbackDuration.value = 1000L
                if (playImmediately) {
                    playAudio(placeholderSurah.audioUrl)
                } else {
                    _playbackState.value = PlaybackState.IDLE
                }
            }
        }
    }

    private suspend fun fetchSurahFromCloudApi(surahId: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        val alquranUrl = "https://api.alquran.cloud/v1/surah/$surahId/editions/quran-uthmani,en.sahih"
        val alquranRequest = Request.Builder().url(alquranUrl).build()

        var arabicEdition: JSONObject? = null
        var englishEdition: JSONObject? = null

        httpClient.newCall(alquranRequest).execute().use { response ->
            if (!response.isSuccessful) throw Exception("AlQuranCloud API returned error code ${response.code}")
            val bodyStr = response.body?.string() ?: throw Exception("Empty API response payload")

            val root = JSONObject(bodyStr)
            val dataArray = root.getJSONArray("data")

            for (j in 0 until dataArray.length()) {
                val editionObj = dataArray.getJSONObject(j)
                val editionMeta = editionObj.optJSONObject("edition")
                val identifier = editionMeta?.optString("identifier") ?: ""
                when (identifier) {
                    "quran-uthmani" -> arabicEdition = editionObj
                    "en.sahih" -> englishEdition = editionObj
                }
            }

            if (arabicEdition == null) arabicEdition = dataArray.optJSONObject(0)
            if (englishEdition == null) englishEdition = dataArray.optJSONObject(1)
        }

        if (arabicEdition == null || englishEdition == null) {
            throw Exception("Required Arabic or English editions were not found in response.")
        }

        val amharicMap = mutableMapOf<Int, String>()
        try {
            val kingFahdUrl = "https://quranenc.com/api/v1/translation/sura/amharic_zain/$surahId"
            val kingFahdRequest = Request.Builder()
                .url(kingFahdUrl)
                .addHeader("User-Agent", "QuranKarimApp/1.0")
                .build()

            httpClient.newCall(kingFahdRequest).execute().use { kfResponse ->
                if (!kfResponse.isSuccessful) {
                    throw Exception("QuranEnc returned error code ${kfResponse.code}")
                }
                val kfBody = kfResponse.body?.string()
                    ?: throw Exception("Empty Amharic translation response")
                val kfRoot = JSONObject(kfBody)
                val resultArray = kfRoot.optJSONArray("result")
                    ?: throw Exception("Amharic translation data was missing")
                for (k in 0 until resultArray.length()) {
                    val item = resultArray.getJSONObject(k)
                    val ayaNum = item.optInt("aya", k + 1)
                    val translation = item.optString("translation", "")
                    if (translation.isNotBlank()) {
                        amharicMap[ayaNum] = translation
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error retrieving King Fahd Amharic translation: ${e.message}", e)
            throw e
        }

        val arabicAyahs = arabicEdition!!.getJSONArray("ayahs")
        val englishAyahs = englishEdition!!.getJSONArray("ayahs")
        val count = arabicAyahs.length()
        if (englishAyahs.length() != count || amharicMap.size < count) {
            throw Exception("Incomplete Arabic, English, or Amharic text for Surah $surahId")
        }
        val list = mutableListOf<AyahEntity>()

        for (i in 0 until count) {
            val arObj = arabicAyahs.getJSONObject(i)
            val enObj = englishAyahs.getJSONObject(i)

            val numInSurah = arObj.getInt("numberInSurah")
            val arText = arObj.getString("text")
            val enText = enObj.getString("text")
            val amText = amharicMap[numInSurah]
                ?: throw Exception("Missing Amharic translation for $surahId:$numInSurah")

            list.add(
                AyahEntity(
                    id = "${surahId}_$numInSurah",
                    surahId = surahId,
                    ayahNumber = numInSurah,
                    arabic = arText,
                    english = enText,
                    amharic = amText,
                    startMs = 0L,
                    endMs = 0L
                )
            )
        }
        list
    }

    private fun mapEntitiesToSurah(header: SurahHeader, entities: List<AyahEntity>, audioUrl: String): Surah {
        val ayahs = entities.map {
            Ayah(
                number = it.ayahNumber,
                arabic = it.arabic,
                english = it.english,
                amharic = it.amharic,
                startMs = 0L,
                endMs = 0L
            )
        }
        
        val estimatedDuration = ayahs.size * 6500L
        val ayahsWithEstimatedTimings = getAyahsWithDynamicTimings(ayahs, estimatedDuration)
        
        return Surah(
            id = header.id,
            name = header.name,
            englishName = header.englishName,
            englishMeaning = header.englishMeaning,
            type = header.type,
            ayahs = ayahsWithEstimatedTimings,
            audioUrl = audioUrl
        )
    }

    private fun getAyahsWithDynamicTimings(ayahs: List<Ayah>, totalDurationMs: Long): List<Ayah> {
        if (ayahs.isEmpty() || totalDurationMs <= 0) return ayahs
        val totalChars = ayahs.sumOf { it.arabic.length }.toDouble()
        var currentStart = 0L
        return ayahs.map { ayah ->
            val weight = ayah.arabic.length.toDouble() / totalChars
            val duration = (weight * totalDurationMs).toLong()
            val end = currentStart + duration
            val updated = ayah.copy(startMs = currentStart, endMs = end)
            currentStart = end
            updated
        }
    }

    fun getAudioUrlCandidates(reciterId: String, surahId: Int): List<String> {
        val formattedId = String.format("%03d.mp3", surahId)
        val candidates = mutableListOf<String>()

        when (reciterId) {
            "yasser" -> {
                candidates.add("https://server11.mp3quran.net/yasser/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/yasser_dussary/$formattedId")
                candidates.add("https://server8.mp3quran.net/yasser/$formattedId")
            }
            "alafasy" -> {
                candidates.add("https://server8.mp3quran.net/afs/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/mishari_al_afasy/$formattedId")
                candidates.add("https://cdn.islamic.network/quran/audio/128/ar.alafasy/$formattedId")
            }
            "sudais" -> {
                candidates.add("https://server11.mp3quran.net/sds/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/abdurrahmaan_as_sudays/$formattedId")
            }
            "shuraim" -> {
                candidates.add("https://server7.mp3quran.net/shur/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/sa'ud_al-shuraym/$formattedId")
            }
            "maher" -> {
                candidates.add("https://server12.mp3quran.net/maher/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/maher_al_muaiqly/$formattedId")
            }
            "baleela" -> {
                candidates.add("https://server6.mp3quran.net/balilah/$formattedId")
            }
            "juhany" -> {
                candidates.add("https://server13.mp3quran.net/jhn/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/abdullaah_awad_al-juhanee/$formattedId")
            }
            "hanif" -> {
                candidates.add("https://server8.mp3quran.net/wd/$formattedId")
            }
            "alijaber" -> {
                candidates.add("https://server11.mp3quran.net/a_jbr/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/ali_jaber/$formattedId")
            }
            "basfar" -> {
                candidates.add("https://server6.mp3quran.net/bsfr/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/abdullah_basfar/$formattedId")
            }
            "subhi" -> {
                candidates.add("https://server14.mp3quran.net/islam/$formattedId")
            }
            "omarhisham" -> {
                omarHishamAudioSources
                    .filter { (_, availableSurahs) -> surahId in availableSurahs }
                    .forEach { (sourceBaseUrl, _) ->
                        candidates.add("$sourceBaseUrl$formattedId")
                    }
                omarHishamAdditionalAudioFiles[surahId]?.let { fileName ->
                    candidates.add("https://ia601807.us.archive.org/23/items/quran-by-omar-hisham-al-arabi/$fileName")
                }
            }
            "misbah_sani" -> {
                candidates.add("https://ia601400.us.archive.org/15/items/002_20260317_202603/$formattedId")
            }
            "afif_taju" -> {
                if (surahId in 1..51) {
                    candidates.add("https://ia601606.us.archive.org/30/items/afiftaj/afif_$surahId.mp3")
                }
                if (surahId in afifTajuAdditionalSurahs) {
                    candidates.add("https://ia600903.us.archive.org/24/items/AfifMohammedTaj/$formattedId")
                }
            }
            "abdulbasit" -> {
                candidates.add("https://server7.mp3quran.net/basit/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/abdul_basit_murattal/$formattedId")
            }
            "minshawi" -> {
                candidates.add("https://server10.mp3quran.net/minsh/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshawi/$formattedId")
            }
            "hussary" -> {
                candidates.add("https://server13.mp3quran.net/hussary/$formattedId")
                candidates.add("https://download.quranicaudio.com/quran/mahmoud_khalil_al-hussary/$formattedId")
            }
            else -> {
                val baseUrl = availableReciters.firstOrNull { it.id == reciterId }?.baseUrl
                    ?: return emptyList()
                candidates.add("$baseUrl$formattedId")
            }
        }
        return candidates
    }

    private fun playAudio(url: String, startPositionMs: Long = 0L) {
        val requestId = audioRequestId.incrementAndGet()
        viewModelScope.launch {
            _playbackState.value = PlaybackState.BUFFERING
            _errorMessage.value = null

            withContext(Dispatchers.IO) {
                try {
                    val customUriStr = _customSurahAudioUris.value[_currentSurah.value.id]
                    val context = getApplication<Application>()
                    val surahId = _currentSurah.value.id
                    val selectedReciterId = _selectedReciter.value.id
                    val networkAvailable = hasValidatedInternet()
                    val localYasserAudio = getDownloadedYasserAudioUri(surahId)
                    val useLocalYasserAudio = localYasserAudio != null &&
                        (_forceOfflineMode.value || selectedReciterId == "yasser" || !networkAvailable)

                    if (_forceOfflineMode.value && customUriStr == null && localYasserAudio == null) {
                        withContext(Dispatchers.Main) {
                            if (audioRequestId.get() == requestId) {
                                _playbackState.value = PlaybackState.ERROR
                                _errorMessage.value =
                                    "Yasser Al-Dosari audio is not downloaded yet. Connect to Wi-Fi to finish the offline download."
                            }
                        }
                        return@withContext
                    }

                    if (!networkAvailable && customUriStr == null && localYasserAudio == null) {
                        withContext(Dispatchers.Main) {
                            if (audioRequestId.get() == requestId) {
                                _playbackState.value = PlaybackState.ERROR
                                _errorMessage.value =
                                    "Offline Yasser Al-Dosari audio is not downloaded for this Surah yet. Connect to Wi-Fi to download it."
                            }
                        }
                        return@withContext
                    }

                    if (customUriStr != null || useLocalYasserAudio) {
                        val mp = MediaPlayer()
                        val localAudioUri = customUriStr?.let(Uri::parse)
                            ?: localYasserAudio
                            ?: throw IllegalStateException("Offline recitation URI became unavailable")
                        mp.setDataSource(context, localAudioUri)
                        mp.prepare()
                        
                        withContext(Dispatchers.Main) {
                            if (audioRequestId.get() == requestId) {
                                setupAndStartMediaPlayer(mp, startPositionMs)
                            } else {
                                mp.release()
                            }
                        }
                    } else {
                        val candidates = getAudioUrlCandidates(selectedReciterId, surahId)
                        val headers = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

                        var success = false
                        for (candidateUrl in candidates) {
                            var testPlayer: MediaPlayer? = null
                            try {
                                Log.d(tag, "Attempting background prepare for candidate: $candidateUrl")
                                testPlayer = MediaPlayer()
                                testPlayer.setDataSource(context, Uri.parse(candidateUrl), headers)
                                testPlayer.prepare() // Synchronous prepare on Dispatchers.IO

                                if (audioRequestId.get() != requestId) {
                                    testPlayer.release()
                                    return@withContext
                                }
                                
                                val duration = testPlayer.duration.toLong()
                                if (duration > 1000L) {
                                    withContext(Dispatchers.Main) {
                                        setupAndStartMediaPlayer(testPlayer, startPositionMs)
                                    }
                                    success = true
                                    Log.d(tag, "Candidate succeeded: $candidateUrl (duration: $duration ms)")
                                    break
                                } else {
                                    testPlayer.release()
                                }
                            } catch (e: Exception) {
                                Log.e(tag, "Candidate failed: $candidateUrl (${e.message})")
                                try { testPlayer?.release() } catch (_: Exception) {}
                            }
                        }

                        if (!success) {
                            withContext(Dispatchers.Main) {
                                if (audioRequestId.get() != requestId) return@withContext
                                Log.e(tag, "All audio candidates failed for ${_selectedReciter.value.name}.")
                                _playbackState.value = PlaybackState.ERROR
                                _errorMessage.value =
                                    "Could not load ${_selectedReciter.value.name}'s audio for ${_currentSurah.value.englishName}. Try another reciter or upload audio for this Surah."
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error in playAudio IO coroutine: ${e.message}")
                    withContext(Dispatchers.Main) {
                        if (audioRequestId.get() != requestId) return@withContext
                        _playbackState.value = PlaybackState.ERROR
                        _errorMessage.value =
                            "Could not play the selected audio file. Choose another file or reciter."
                    }
                }
            }
        }
    }

    private fun setupAndStartMediaPlayer(mp: MediaPlayer, startPositionMs: Long = 0L) {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {}

        mediaPlayer = mp
        val duration = mp.duration.toLong().coerceAtLeast(1000L)
        _playbackDuration.value = duration
        _playbackState.value = PlaybackState.PLAYING

        val surah = _currentSurah.value
        val updatedAyahs = getAyahsWithDynamicTimings(surah.ayahs, duration)
        _currentSurah.value = surah.copy(ayahs = updatedAyahs)
        val startPosition = startPositionMs.coerceIn(0L, duration - 1L)
        if (startPosition > 0L) mp.seekTo(startPosition.toInt())
        _playbackPosition.value = startPosition
        updateActiveAyah(startPosition)

        mp.setOnCompletionListener {
            Log.d(tag, "MediaPlayer completed playing active Surah.")
            handleSurahCompletion()
        }

        mp.setOnErrorListener { _, what, extra ->
            Log.e(tag, "MediaPlayer playback error: what=$what, extra=$extra")
            _playbackState.value = PlaybackState.ERROR
            _errorMessage.value =
                "Audio playback failed for ${_selectedReciter.value.name}. Try another reciter or upload audio for this Surah."
            true
        }

        applyPlaybackSpeed()
        mp.start()
        startProgressTracking()
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        if (current == PlaybackState.PLAYING) {
            pausePlayback()
        } else if (current == PlaybackState.PAUSED) {
            resumePlayback()
        } else {
            playAudio(_currentSurah.value.audioUrl)
        }
    }

    private fun pausePlayback() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
            _playbackState.value = PlaybackState.PAUSED
            progressJob?.cancel()
        } catch (e: Exception) {
            Log.e(tag, "Error pausing media: ${e.message}")
        }
    }

    private fun resumePlayback() {
        try {
            mediaPlayer?.start()
            _playbackState.value = PlaybackState.PLAYING
            startProgressTracking()
        } catch (e: Exception) {
            Log.e(tag, "Error resuming media: ${e.message}")
            playAudio(_currentSurah.value.audioUrl)
        }
    }

    fun stopPlayback() {
        audioRequestId.incrementAndGet()
        try {
            progressJob?.cancel()
            mockTimerJob?.cancel()
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            _playbackState.value = PlaybackState.IDLE
            _playbackPosition.value = 0L
            _activeAyahNumber.value = null
        } catch (e: Exception) {
            Log.e(tag, "Error stopping media: ${e.message}")
        }
    }

    fun seekTo(positionMs: Long) {
        val clampedPosition = positionMs.coerceIn(0L, _playbackDuration.value)
        _playbackPosition.value = clampedPosition
        updateActiveAyah(clampedPosition)

        if (_playbackState.value == PlaybackState.ERROR) {
            return
        }

        try {
            mediaPlayer?.seekTo(clampedPosition.toInt())
        } catch (e: Exception) {
            Log.e(tag, "Error seeking: ${e.message}")
        }
    }

    fun selectAyah(ayah: Ayah) {
        playAudio(_currentSurah.value.audioUrl, ayah.startMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        _playbackSpeed.value = clamped
        sharedPrefs.edit().putFloat("playback_speed", clamped).apply()
        applyPlaybackSpeed()
    }

    private fun applyPlaybackSpeed() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer?.let {
                    if (it.isPlaying || _playbackState.value == PlaybackState.PAUSED) {
                        val params = it.playbackParams
                        params.speed = _playbackSpeed.value
                        it.playbackParams = params
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error setting playback speed params: ${e.message}")
        }
    }

    fun skipNext() {
        val currentId = _currentSurah.value.id
        if (currentId < 114) {
            loadSurah(currentId + 1, playImmediately = true)
        } else {
            loadSurah(1, playImmediately = true)
        }
    }

    fun skipPrevious() {
        val currentId = _currentSurah.value.id
        if (currentId > 1) {
            loadSurah(currentId - 1, playImmediately = true)
        } else {
            loadSurah(114, playImmediately = true)
        }
    }

    private fun handleSurahCompletion() {
        skipNext()
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_playbackState.value == PlaybackState.PLAYING) {
                try {
                    val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    _playbackPosition.value = pos
                    updateActiveAyah(pos)
                } catch (e: Exception) {
                    Log.e(tag, "Error getting playback position: ${e.message}")
                }
                delay(100)
            }
        }
    }

    // Pure Quran audio tracking without any background beep / noise sound!
    private fun updateActiveAyah(positionMs: Long) {
        val active = _currentSurah.value.ayahs.firstOrNull {
            positionMs >= it.startMs && positionMs < it.endMs
        }
        val nextNumber = active?.number
        if (nextNumber != _activeAyahNumber.value) {
            _activeAyahNumber.value = nextNumber
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setArabicFontSize(size: Float) {
        val clamped = size.coerceIn(20f, 44f)
        _arabicFontSize.value = clamped
        sharedPrefs.edit().putFloat("arabic_font_size", clamped).apply()
    }

    fun setTranslationFontSize(size: Float) {
        val clamped = size.coerceIn(12f, 26f)
        _translationFontSize.value = clamped
        sharedPrefs.edit().putFloat("translation_font_size", clamped).apply()
    }

    fun setReadingLayout(layout: ReadingLayout) {
        _readingLayout.value = layout
        sharedPrefs.edit().putString("reading_layout", layout.name).apply()
    }

    fun toggleAyahBookmark(surahId: Int, ayahNumber: Int) {
        val header = QuranRepository.allSurahHeaders.firstOrNull { it.id == surahId } ?: return
        if (ayahNumber !in 1..header.numberOfAyahs) return

        val bookmark = AyahBookmark(surahId, ayahNumber)
        val updated = _bookmarkedAyahs.value.toMutableSet().apply {
            if (!add(bookmark)) remove(bookmark)
        }
        _bookmarkedAyahs.value = updated
        sharedPrefs.edit().putStringSet(
            "bookmarked_ayahs",
            updated.map { "${it.surahId}:${it.ayahNumber}" }.toSet()
        ).apply()
    }

    private fun loadBookmarkedAyahs(): Set<AyahBookmark> =
        sharedPrefs.getStringSet("bookmarked_ayahs", emptySet())
            .orEmpty()
            .mapNotNull { value ->
                val parts = value.split(':')
                val surahId = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val ayahNumber = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val header = QuranRepository.allSurahHeaders.firstOrNull { it.id == surahId }
                if (header != null && ayahNumber in 1..header.numberOfAyahs) {
                    AyahBookmark(surahId, ayahNumber)
                } else {
                    null
                }
            }.toSet()

    fun setSelectedTafsirType(type: String) {
        _selectedTafsirType.value = type
        val current = _currentTafsir.value
        if (current != null) {
            fetchTafsirForAyah(current.surahId, current.ayahNumber, type)
        }
    }

    fun fetchTafsirForAyah(surahId: Int, ayahNumber: Int, tafsirType: String = _selectedTafsirType.value) {
        _isTafsirLoading.value = true
        _isCurrentTafsirSaved.value = false
        _selectedTafsirType.value = tafsirType

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cached = quranDao.getTafsir(tafsirType, surahId, ayahNumber)
                val tafsirText = cached?.tafsirText ?: downloadTafsirText(surahId, ayahNumber, tafsirType)
                val data = createTafsirData(surahId, ayahNumber, tafsirType, tafsirText)
                withContext(Dispatchers.Main) {
                    _currentTafsir.value = data
                    _isCurrentTafsirSaved.value = cached != null
                    _isTafsirLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving Tafsir: ${e.message}")
                withContext(Dispatchers.Main) {
                    _isTafsirLoading.value = false
                    _currentTafsir.value = TafsirData(
                        tafsirType = tafsirType,
                        tafsirName = if (tafsirType == "saadi") "تفسير السعدي" else "تفسير ابن كثير",
                        authorName = if (tafsirType == "saadi") "الشيخ عبد الرحمن بن ناصر السعدي" else "الحافظ ابن كثير",
                        surahId = surahId,
                        ayahNumber = ayahNumber,
                        arabicText = "",
                        tafsirText = "Unable to connect to Quran.com Tafsir API. Please ensure your device has internet access."
                    )
                }
            }
        }
    }

    fun saveCurrentTafsirForOffline() {
        val tafsir = _currentTafsir.value ?: return
        if (tafsir.tafsirText.startsWith("Unable to connect")) {
            _errorMessage.value = "This tafsir could not be downloaded. Check your internet connection and try again."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                quranDao.insertTafsir(
                    TafsirEntity(
                        id = "${tafsir.tafsirType}:${tafsir.surahId}:${tafsir.ayahNumber}",
                        surahId = tafsir.surahId,
                        ayahNumber = tafsir.ayahNumber,
                        tafsirType = tafsir.tafsirType,
                        tafsirText = tafsir.tafsirText
                    )
                )
                _isCurrentTafsirSaved.value = true
            } catch (exception: Exception) {
                Log.e(tag, "Could not save tafsir for offline use", exception)
                _errorMessage.value = "Could not save this tafsir for offline reading."
            }
        }
    }

    private fun downloadTafsirText(surahId: Int, ayahNumber: Int, tafsirType: String): String {
        val resourceId = if (tafsirType == "saadi") 91 else 14
        val url = "https://api.quran.com/api/v4/tafsirs/$resourceId/by_ayah/$surahId:$ayahNumber"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "QuranKarimApp/1.0")
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Tafsir API returned code ${response.code}")
            val bodyStr = response.body?.string() ?: throw Exception("Empty Tafsir payload")
            val root = JSONObject(bodyStr)
            val tafsirObj = root.optJSONObject("tafsir")
                ?: throw Exception("Tafsir content unavailable for verse $surahId:$ayahNumber")
            android.text.Html.fromHtml(
                tafsirObj.optString("text", ""),
                android.text.Html.FROM_HTML_MODE_LEGACY
            ).toString().trim()
        }
    }

    private fun createTafsirData(
        surahId: Int,
        ayahNumber: Int,
        tafsirType: String,
        tafsirText: String
    ): TafsirData {
        val activeAyah = _currentSurah.value.takeIf { it.id == surahId }
            ?.ayahs?.firstOrNull { it.number == ayahNumber }
        return TafsirData(
            tafsirType = tafsirType,
            tafsirName = if (tafsirType == "saadi") {
                "تيسير الكريم الرحمن في تفسير كلام المنان (السعدي)"
            } else {
                "تفسير القرآن العظيم (ابن كثير)"
            },
            authorName = if (tafsirType == "saadi") {
                "الشيخ عبد الرحمن بن ناصر السعدي"
            } else {
                "الحافظ ابن كثير"
            },
            surahId = surahId,
            ayahNumber = ayahNumber,
            arabicText = activeAyah?.arabic.orEmpty(),
            tafsirText = tafsirText
        )
    }

    fun clearTafsir() {
        _currentTafsir.value = null
        _isCurrentTafsirSaved.value = false
    }

    private var mockTimerJob: Job? = null
    private fun startMockPlaybackSimulation() {
        mockTimerJob?.cancel()
        _playbackState.value = PlaybackState.PLAYING
        val totalDuration = _currentSurah.value.ayahs.lastOrNull()?.endMs ?: 35000L
        _playbackDuration.value = totalDuration

        mockTimerJob = viewModelScope.launch {
            var elapsed = _playbackPosition.value
            while (_playbackState.value == PlaybackState.PLAYING) {
                delay(100)
                elapsed += (100 * _playbackSpeed.value).toLong()
                if (elapsed >= totalDuration) {
                    _playbackPosition.value = totalDuration
                    updateActiveAyah(totalDuration)
                    handleSurahCompletion()
                    break
                }
                _playbackPosition.value = elapsed
                updateActiveAyah(elapsed)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        mockTimerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
