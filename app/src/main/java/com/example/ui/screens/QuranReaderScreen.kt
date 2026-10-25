package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.Ayah
import com.example.data.QuranRepository
import com.example.data.Surah
import com.example.data.SurahHeader
import com.example.ui.screens.CreatorContactLink
import com.example.viewmodel.AppTheme
import com.example.viewmodel.AyahBookmark
import com.example.viewmodel.PlaybackState
import com.example.viewmodel.QuranViewModel
import com.example.viewmodel.ReadingLayout
import com.example.viewmodel.Reciter
import com.example.viewmodel.TafsirData
import kotlinx.coroutines.delay

data class ThemeColors(
    val bg: Color,
    val cardBg: Color,
    val topBarBg: Color,
    val tabBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentGreen: Color,
    val gold: Color
)

fun getThemeColors(theme: AppTheme): ThemeColors {
    return when (theme) {
        AppTheme.EMERALD_GREEN -> ThemeColors(
            bg = Color(0xFF071F1A),
            cardBg = Color(0xFF10332B),
            topBarBg = Color(0xFF0A2B24),
            tabBg = Color(0xFF0D332B),
            textPrimary = Color.White,
            textSecondary = Color(0xFFA0C4B8),
            accentGreen = Color(0xFF10B981),
            gold = Color(0xFFFBBF24)
        )
        AppTheme.DARK_BLACK -> ThemeColors(
            bg = Color(0xFF121212),
            cardBg = Color(0xFF1E1E1E),
            topBarBg = Color(0xFF1E1E1E),
            tabBg = Color(0xFF181818),
            textPrimary = Color.White,
            textSecondary = Color.Gray,
            accentGreen = Color(0xFF10B981),
            gold = Color(0xFFD4AF37)
        )
        AppTheme.LIGHT -> ThemeColors(
            bg = Color(0xFFF4F7F5),
            cardBg = Color(0xFFFFFFFF),
            topBarBg = Color(0xFFE2EBE6),
            tabBg = Color(0xFFE2EBE6),
            textPrimary = Color(0xFF111827),
            textSecondary = Color(0xFF4B5563),
            accentGreen = Color(0xFF059669),
            gold = Color(0xFFB45309)
        )
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    viewModel: QuranViewModel,
    appLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onOpenStartup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playlist by viewModel.currentPlaylist.collectAsState()
    val selectedSurah by viewModel.currentSurah.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val playbackDuration by viewModel.playbackDuration.collectAsState()
    val activeAyahNumber by viewModel.activeAyahNumber.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val translationFontSize by viewModel.translationFontSize.collectAsState()
    val readingLayout by viewModel.readingLayout.collectAsState()
    val forceOfflineMode by viewModel.forceOfflineMode.collectAsState()
    val autoScrollCentering by viewModel.autoScrollCentering.collectAsState()
    val customSurahAudioUris by viewModel.customSurahAudioUris.collectAsState()

    val currentTafsir by viewModel.currentTafsir.collectAsState()
    val isTafsirLoading by viewModel.isTafsirLoading.collectAsState()
    val isCurrentTafsirSaved by viewModel.isCurrentTafsirSaved.collectAsState()
    val selectedTafsirType by viewModel.selectedTafsirType.collectAsState()
    val textDownloadProgress by viewModel.textDownloadProgress.collectAsState()
    val audioDownloadProgress by viewModel.audioDownloadProgress.collectAsState()
    val audioDownloadRequested by viewModel.audioDownloadRequested.collectAsState()
    val bookmarkedAyahs by viewModel.bookmarkedAyahs.collectAsState()

    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var isReadingViewActive by rememberSaveable { mutableStateOf(false) }
    var showReaderMenu by rememberSaveable { mutableStateOf(false) }
    var slowAutoScroll by rememberSaveable { mutableStateOf(false) }
    val readerStateHolder = rememberSaveableStateHolder()
    var bookmarkedSurahIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val colors = getThemeColors(appTheme)
    val view = LocalView.current
    val window = LocalContext.current.findActivity()?.window
    val isCompactReaderToolbar = LocalConfiguration.current.screenWidthDp < 400
    val useImmersiveQuranReader = isReadingViewActive &&
            readingLayout == ReadingLayout.ARABIC_ONLY &&
            currentTafsir == null &&
            !isTafsirLoading &&
            !showSettingsDialog

    BackHandler(
        enabled = isReadingViewActive &&
                !showSettingsDialog &&
                currentTafsir == null &&
                !isTafsirLoading &&
                !showReaderMenu
    ) {
        isReadingViewActive = false
    }

    DisposableEffect(window, view, useImmersiveQuranReader) {
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (useImmersiveQuranReader) {
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(readingLayout) {
        if (readingLayout != ReadingLayout.ARABIC_ONLY) slowAutoScroll = false
    }

    // Animated Zoom Entrance
    val pageZoomScale by animateFloatAsState(
        targetValue = if (isReadingViewActive) 1.0f else 0.92f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "PageZoomAnimation"
    )

    // Auto-scroll active verse
    LaunchedEffect(activeAyahNumber) {
        val activeNum = activeAyahNumber
        if (activeNum != null && isReadingViewActive) {
            val index = selectedSurah.ayahs.indexOfFirst { it.number == activeNum }
            if (index >= 0 && !listState.isScrollInProgress) {
                if (autoScrollCentering) {
                    val viewportHeight = listState.layoutInfo.viewportSize.height
                    val offset = if (viewportHeight > 0) -viewportHeight / 3 else -300
                    listState.animateScrollToItem(index, scrollOffset = offset)
                } else {
                    listState.animateScrollToItem(index)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (isReadingViewActive &&
                (readingLayout != ReadingLayout.ARABIC_ONLY || playbackState != PlaybackState.IDLE)
            ) {
                ExpandedAudioControlBar(
                    viewModel = viewModel,
                    selectedSurah = selectedSurah,
                    selectedReciter = selectedReciter,
                    playbackState = playbackState,
                    playbackPosition = playbackPosition,
                    playbackDuration = playbackDuration,
                    playbackSpeed = playbackSpeed,
                    colors = colors,
                    onOpenSettings = { showSettingsDialog = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
        ) {
            if (!isReadingViewActive) {
                // Directory / Index View
                readerStateHolder.SaveableStateProvider("directory") {
                    DirectoryIndexView(
                        playlist = playlist,
                        bookmarkedSurahIds = bookmarkedSurahIds,
                        colors = colors,
                        bookmarkedAyahs = bookmarkedAyahs,
                        onSelectSurah = { surahHeader ->
                            viewModel.loadSurah(surahHeader.id, playImmediately = false)
                            isReadingViewActive = true
                        },
                        onSelectAyahBookmark = { bookmark ->
                            viewModel.loadSurah(
                                surahId = bookmark.surahId,
                                playImmediately = false,
                                initialAyahNumber = bookmark.ayahNumber
                            )
                            isReadingViewActive = true
                        },
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenStartup = onOpenStartup
                    )
                }
            } else {
                // Full Page / Quran Reading View
                readerStateHolder.SaveableStateProvider("reading") {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pageZoomScale)
                    ) {
                    val header = QuranRepository.allSurahHeaders.firstOrNull { it.id == selectedSurah.id }
                    val pageNum = header?.pageNumber ?: 1
                    val juzNum = header?.juzNumber ?: 1
                    val isActiveAyahBookmarked = activeAyahNumber?.let { ayahNumber ->
                        AyahBookmark(selectedSurah.id, ayahNumber) in bookmarkedAyahs
                    } == true
                    val isBookmarked = if (activeAyahNumber != null) {
                        isActiveAyahBookmarked
                    } else {
                        bookmarkedSurahIds.contains(selectedSurah.id)
                    }

                    // Top App Bar
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = appText("surah", selectedSurah.englishName.removePrefix("Surah ")),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = appText("pageJuz", pageNum, juzNum),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { isReadingViewActive = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = appText("backDirectory"),
                                    tint = colors.textPrimary
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.setArabicFontSize((arabicFontSize - 2f).coerceAtLeast(20f)) }
                            ) {
                                Text("A-", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            IconButton(
                                onClick = { viewModel.setArabicFontSize((arabicFontSize + 2f).coerceAtMost(44f)) }
                            ) {
                                Text("A+", color = colors.accentGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            IconButton(
                                onClick = {
                                    val ayahNumber = activeAyahNumber
                                    if (ayahNumber != null) {
                                        viewModel.toggleAyahBookmark(selectedSurah.id, ayahNumber)
                                    } else {
                                        bookmarkedSurahIds = if (isBookmarked) {
                                            bookmarkedSurahIds - selectedSurah.id
                                        } else {
                                            bookmarkedSurahIds + selectedSurah.id
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (activeAyahNumber != null) {
                                        appText("bookmarkAyah")
                                    } else {
                                        appText("bookmark")
                                    },
                                    tint = if (isBookmarked) colors.gold else colors.textPrimary
                                )
                            }

                            if (!isCompactReaderToolbar) {
                                IconButton(
                                    onClick = {
                                        onLanguageSelected(
                                            if (appLanguage == AppLanguage.AMHARIC) {
                                                AppLanguage.ENGLISH
                                            } else {
                                                AppLanguage.AMHARIC
                                            }
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = appText("languageIcon"),
                                        tint = colors.textPrimary
                                    )
                                }
                            }

                            Box {
                                IconButton(onClick = { showReaderMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = appText("menu"),
                                        tint = colors.textPrimary
                                    )
                                }

                                DropdownMenu(
                                    expanded = showReaderMenu,
                                    onDismissRequest = { showReaderMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(appText("readerSettings")) },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        onClick = {
                                            showReaderMenu = false
                                            showSettingsDialog = true
                                        }
                                    )
                                    if (isCompactReaderToolbar) {
                                        DropdownMenuItem(
                                            text = { Text(appText("languageIcon")) },
                                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                            onClick = {
                                                showReaderMenu = false
                                                onLanguageSelected(
                                                    if (appLanguage == AppLanguage.AMHARIC) {
                                                        AppLanguage.ENGLISH
                                                    } else {
                                                        AppLanguage.AMHARIC
                                                    }
                                                )
                                            }
                                        )
                                    }
                                    if (readingLayout == ReadingLayout.ARABIC_ONLY) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    appText(
                                                        if (slowAutoScroll) "slowScrollStop" else "slowScrollStart"
                                                    )
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    if (slowAutoScroll) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                slowAutoScroll = !slowAutoScroll
                                                showReaderMenu = false
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text(appText("readingLayout")) },
                                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                                        onClick = {
                                            showReaderMenu = false
                                            val nextLayout = when (readingLayout) {
                                                ReadingLayout.ARABIC_ONLY -> ReadingLayout.STACKED
                                                ReadingLayout.STACKED -> ReadingLayout.PARALLEL
                                                ReadingLayout.PARALLEL -> ReadingLayout.AUDIO
                                                ReadingLayout.AUDIO -> ReadingLayout.ARABIC_ONLY
                                            }
                                            viewModel.setReadingLayout(nextLayout)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(appText("startupScreen")) },
                                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                        onClick = {
                                            showReaderMenu = false
                                            onOpenStartup()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.topBarBg
                        )
                    )

                    // Loading State
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.accentGreen)
                        }
                    } else if (readingLayout == ReadingLayout.ARABIC_ONLY) {
                        // Continuous Full Page Quran Mushaf View
                        FullPageMushafView(
                            surah = selectedSurah,
                            arabicFontSize = arabicFontSize,
                            activeAyahNumber = activeAyahNumber,
                            slowAutoScroll = slowAutoScroll,
                            onSlowAutoScrollChange = { slowAutoScroll = it },
                            colors = colors,
                            onAyahClick = { ayah -> viewModel.selectAyah(ayah) },
                            onTafsirClick = { ayah -> viewModel.fetchTafsirForAyah(selectedSurah.id, ayah.number) }
                        )
                    } else {
                        // Verse-by-Verse Stacked / Parallel View
                        AyahListView(
                            surahId = selectedSurah.id,
                            ayahs = selectedSurah.ayahs,
                            activeAyahNumber = activeAyahNumber,
                            arabicFontSize = arabicFontSize,
                            translationFontSize = translationFontSize,
                            readingLayout = readingLayout,
                            listState = listState,
                            selectedLanguage = selectedLanguage,
                            bookmarkedAyahs = bookmarkedAyahs,
                            colors = colors,
                            onAyahClick = { viewModel.selectAyah(it) },
                            onTafsirClick = { ayah -> viewModel.fetchTafsirForAyah(selectedSurah.id, ayah.number) },
                            onToggleAyahBookmark = { ayah -> viewModel.toggleAyahBookmark(selectedSurah.id, ayah.number) }
                        )
                    }
                    }
                }
            }

            // Settings Dialog
            if (showSettingsDialog) {
                SettingsCustomizerDialog(
                    viewModel = viewModel,
                    forceOfflineMode = forceOfflineMode,
                    autoScrollCentering = autoScrollCentering,
                    arabicFontSize = arabicFontSize,
                    translationFontSize = translationFontSize,
                    readingLayout = readingLayout,
                    customSurahAudioUris = customSurahAudioUris,
                    selectedLanguage = selectedLanguage,
                    selectedReciter = selectedReciter,
                    appLanguage = appLanguage,
                    onLanguageSelected = onLanguageSelected,
                    textDownloadProgress = textDownloadProgress,
                    audioDownloadProgress = audioDownloadProgress,
                    audioDownloadRequested = audioDownloadRequested,
                    appTheme = appTheme,
                    colors = colors,
                    allSurahs = QuranRepository.allSurahHeaders,
                    currentSurahId = selectedSurah.id,
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Tafsir Dialog
            if (currentTafsir != null || isTafsirLoading) {
                TafsirDialog(
                    tafsirData = currentTafsir,
                    isLoading = isTafsirLoading,
                    isSavedForOffline = isCurrentTafsirSaved,
                    selectedTafsirType = selectedTafsirType,
                    colors = colors,
                    onSelectTafsirType = { viewModel.setSelectedTafsirType(it) },
                    onSaveForOffline = viewModel::saveCurrentTafsirForOffline,
                    onDismiss = { viewModel.clearTafsir() }
                )
            }
        }
    }
}

// Clean helper function to strip integrated Bismillah from Ayah 1 text
fun cleanSeparatedAyahText(surahId: Int, ayahNumber: Int, text: String): String {
    if (surahId == 1 || surahId == 9 || ayahNumber != 1) return text
    val bismillahPrefixes = listOf(
        "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيمِ",
        "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
        "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
    )
    var cleaned = text.trim()
    for (prefix in bismillahPrefixes) {
        if (cleaned.startsWith(prefix)) {
            cleaned = cleaned.substring(prefix.length).trim()
            break
        }
    }
    return cleaned.ifEmpty { text }
}

// Directory / Main Index Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryIndexView(
    playlist: List<SurahHeader>,
    bookmarkedSurahIds: Set<Int>,
    bookmarkedAyahs: Set<AyahBookmark>,
    colors: ThemeColors,
    onSelectSurah: (SurahHeader) -> Unit,
    onSelectAyahBookmark: (AyahBookmark) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStartup: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearching by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isSearching) {
        if (searchQuery.isNotBlank()) {
            searchQuery = ""
        } else {
            isSearching = false
        }
    }

    val filteredPlaylist = remember(playlist, searchQuery) {
        if (searchQuery.isBlank()) playlist
        else playlist.filter {
            it.englishName.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery) ||
                    it.id.toString() == searchQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                if (isSearching) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(appText("searchSurah"), color = colors.textSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentGreen,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Quran Karim", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (isSearching) {
                            searchQuery = ""
                            isSearching = false
                        } else {
                            isSearching = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = appText("search"),
                        tint = colors.textPrimary
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = appText("reader"), tint = colors.textPrimary)
                }

                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = appText("more"), tint = colors.textPrimary)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(appText("settings")) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(appText("startup")) },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenStartup()
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.topBarBg)
        )

        // Developer Credit Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGreen.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = appText("creatorCredit"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentGreen
                )
                CreatorContactLink(color = colors.textSecondary)
            }
        }

        // Top Tabs: SURAHS, JUZ', BOOKMARKS
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.tabBg,
            contentColor = colors.accentGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.accentGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(appText("surahs"), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = colors.accentGreen,
                unselectedContentColor = colors.textSecondary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(appText("juz"), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = colors.accentGreen,
                unselectedContentColor = colors.textSecondary
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(appText("bookmarks"), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = colors.accentGreen,
                unselectedContentColor = colors.textSecondary
            )
        }

        when (selectedTab) {
            0 -> SurahsTabContent(playlist = filteredPlaylist, colors = colors, onSelectSurah = onSelectSurah)
            1 -> JuzTabContent(playlist = playlist, colors = colors, onSelectSurah = onSelectSurah)
            2 -> BookmarksTabContent(
                playlist = playlist,
                bookmarkedSurahIds = bookmarkedSurahIds,
                bookmarkedAyahs = bookmarkedAyahs,
                colors = colors,
                onSelectSurah = onSelectSurah,
                onSelectAyahBookmark = onSelectAyahBookmark
            )
        }
    }
}

@Composable
fun SurahsTabContent(
    playlist: List<SurahHeader>,
    colors: ThemeColors,
    onSelectSurah: (SurahHeader) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        var lastJuz = -1
        playlist.forEach { header ->
            if (header.juzNumber != lastJuz) {
                lastJuz = header.juzNumber
                item(key = "juz_header_${header.juzNumber}") {
                    JuzHeaderRow(juzNumber = header.juzNumber, pageNumber = header.pageNumber, colors = colors)
                }
            }
            item(key = "surah_${header.id}") {
                SurahListItem(header = header, colors = colors, onClick = { onSelectSurah(header) })
            }
        }
    }
}

@Composable
fun JuzHeaderRow(juzNumber: Int, pageNumber: Int, colors: ThemeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.topBarBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appText("juzNumber", juzNumber),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = "$pageNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
fun SurahListItem(
    header: SurahHeader,
    colors: ThemeColors,
    onClick: () -> Unit
) {
    val surahType = when (header.type.lowercase()) {
        "makki", "meccan" -> appText("meccan")
        "madani", "medinan" -> appText("medinan")
        else -> header.type
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${header.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                modifier = Modifier.width(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = header.englishName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = appText("verses", surahType, header.numberOfAyahs),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        Text(
            text = "${header.pageNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
fun JuzTabContent(
    playlist: List<SurahHeader>,
    colors: ThemeColors,
    onSelectSurah: (SurahHeader) -> Unit
) {
    val juzMap = remember(playlist) {
        playlist.groupBy { it.juzNumber }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        items((1..30).toList()) { juz ->
            val firstSurah = juzMap[juz]?.firstOrNull() ?: playlist[0]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onSelectSurah(firstSurah) },
                colors = CardDefaults.cardColors(containerColor = colors.cardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(appText("juzNumber", juz), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(appText("startsAtSurah", firstSurah.englishName), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Text(appText("pageNumber", firstSurah.pageNumber), style = MaterialTheme.typography.bodyMedium, color = colors.accentGreen)
                }
            }
        }
    }
}

@Composable
fun BookmarksTabContent(
    playlist: List<SurahHeader>,
    bookmarkedSurahIds: Set<Int>,
    bookmarkedAyahs: Set<AyahBookmark>,
    colors: ThemeColors,
    onSelectSurah: (SurahHeader) -> Unit,
    onSelectAyahBookmark: (AyahBookmark) -> Unit
) {
    val bookmarked = playlist.filter { bookmarkedSurahIds.contains(it.id) }
    val savedAyahs = bookmarkedAyahs.sortedWith(compareBy({ it.surahId }, { it.ayahNumber }))
    if (bookmarked.isEmpty() && savedAyahs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(colors.bg), contentAlignment = Alignment.Center) {
            Text(appText("noBookmarks"), color = colors.textSecondary)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            items(savedAyahs, key = { "ayah_${it.surahId}_${it.ayahNumber}" }) { bookmark ->
                val header = playlist.firstOrNull { it.id == bookmark.surahId }
                if (header != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                            .clickable { onSelectAyahBookmark(bookmark) },
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.gold.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = colors.gold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(header.englishName, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                                Text(
                                    appText("ayahBookmarkRow", bookmark.ayahNumber),
                                    color = colors.textSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            items(bookmarked) { header ->
                SurahListItem(header = header, colors = colors, onClick = { onSelectSurah(header) })
            }
        }
    }
}

// Continuous Full Page Mushaf View with Separated Bismillah!
@Composable
fun FullPageMushafView(
    surah: Surah,
    arabicFontSize: Float,
    activeAyahNumber: Int?,
    slowAutoScroll: Boolean,
    onSlowAutoScrollChange: (Boolean) -> Unit,
    colors: ThemeColors,
    onAyahClick: (Ayah) -> Unit,
    onTafsirClick: (Ayah) -> Unit
) {
    val scrollState = rememberScrollState()
    var textLayoutResult by remember(surah.id) { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    LaunchedEffect(scrollState, slowAutoScroll) {
        while (slowAutoScroll && scrollState.value < scrollState.maxValue) {
            scrollState.scrollBy(0.5f)
            delay(16)
        }
        if (slowAutoScroll && scrollState.value >= scrollState.maxValue) {
            onSlowAutoScrollChange(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .pointerInput(slowAutoScroll) {
                awaitEachGesture {
                    awaitFirstDown()
                    if (slowAutoScroll) onSlowAutoScrollChange(false)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Distinct Separated Bismillah Card Header
        if (surah.id != 9) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.gold.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = (arabicFontSize * 0.95f).sp
                    ),
                    color = colors.gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Inline Annotated Flowing Full Page Script
        val annotatedText = buildAnnotatedString {
            surah.ayahs.forEach { ayah ->
                val isActive = ayah.number == activeAyahNumber
                val cleanArabic = cleanSeparatedAyahText(surah.id, ayah.number, ayah.arabic)

                pushStringAnnotation(tag = "AYAH", annotation = ayah.number.toString())
                withStyle(
                    style = SpanStyle(
                        color = if (isActive) colors.gold else colors.textPrimary,
                        fontSize = arabicFontSize.sp,
                        fontFamily = FontFamily.Serif
                    )
                ) {
                    append(cleanArabic)
                    append(" ")
                }

                // Inline green circular verse number
                appendInlineContent(id = "verse_${ayah.number}", alternateText = "(${ayah.number})")
                append("  ")
                pop()
            }
        }

        val inlineContent = remember(surah.ayahs, colors, activeAyahNumber) {
            surah.ayahs.associate { ayah ->
                "verse_${ayah.number}" to InlineTextContent(
                    Placeholder(
                        width = (arabicFontSize * 1.3f).sp,
                        height = (arabicFontSize * 1.3f).sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    InlineVerseNumberCircle(number = ayah.number, isActive = ayah.number == activeAyahNumber, colors = colors)
                }
            }
        }

        Text(
            text = annotatedText,
            inlineContent = inlineContent,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(annotatedText) {
                    detectTapGestures { tap ->
                        val offset = textLayoutResult?.getOffsetForPosition(tap) ?: return@detectTapGestures
                        val ayahNumber = annotatedText
                            .getStringAnnotations("AYAH", offset, offset)
                            .firstOrNull()
                            ?.item
                            ?.toIntOrNull()
                        surah.ayahs.firstOrNull { it.number == ayahNumber }?.let(onAyahClick)
                    }
                },
            onTextLayout = { textLayoutResult = it },
            textAlign = TextAlign.Right,
            lineHeight = (arabicFontSize * 1.85f).sp,
            style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl)
        )

    }
}

// Circular Green Inline Verse Number Circle
@Composable
fun InlineVerseNumberCircle(number: Int, isActive: Boolean, colors: ThemeColors) {
    val circleColor = if (isActive) colors.gold else colors.accentGreen

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(circleColor.copy(alpha = 0.2f))
            .border(1.5.dp, circleColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = circleColor
        )
    }
}

// Expanded Bottom Audio Controls Bar with Play / Pause / Resume / Stop / Seek Slider
@Composable
fun ExpandedAudioControlBar(
    viewModel: QuranViewModel,
    selectedSurah: Surah,
    selectedReciter: Reciter,
    playbackState: PlaybackState,
    playbackPosition: Long,
    playbackDuration: Long,
    playbackSpeed: Float,
    colors: ThemeColors,
    onOpenSettings: () -> Unit
) {
    val progress = remember(playbackPosition, playbackDuration) {
        if (playbackDuration > 0) playbackPosition.toFloat() / playbackDuration else 0f
    }

    Surface(
        color = colors.topBarBg,
        tonalElevation = 12.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGreen.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Seek Progress Slider
            Slider(
                value = progress,
                onValueChange = { percent ->
                    val pos = (percent * playbackDuration).toLong()
                    viewModel.seekTo(pos)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                colors = SliderDefaults.colors(
                    thumbColor = colors.accentGreen,
                    activeTrackColor = colors.accentGreen,
                    inactiveTrackColor = colors.accentGreen.copy(alpha = 0.2f)
                )
            )

            // Duration details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(playbackPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = formatTime(playbackDuration),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            // Audio Player Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentGreen.copy(alpha = 0.2f))
                        .clickable {
                            val next = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 2.0f
                                2.0f -> 0.75f
                                else -> 1.0f
                            }
                            viewModel.setPlaybackSpeed(next)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("${playbackSpeed}x", color = colors.accentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Playback Buttons: Prev, Play/Pause, Stop, Next
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Prev Surah
                    IconButton(onClick = { viewModel.skipPrevious() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = appText("previous"), tint = colors.textPrimary)
                    }

                    // Main Play / Pause / Resume Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(colors.accentGreen)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (playbackState == PlaybackState.BUFFERING) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
                        } else {
                            Icon(
                                imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = appText("playPause"),
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Stop Button
                    IconButton(onClick = { viewModel.stopPlayback() }) {
                        Icon(Icons.Default.Stop, contentDescription = appText("stop"), tint = colors.textPrimary)
                    }

                    // Next Surah
                    IconButton(onClick = { viewModel.skipNext() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = appText("next"), tint = colors.textPrimary)
                    }
                }

                // Reciter Info / Expand Button
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Tune, contentDescription = appText("reciterSettings"), tint = colors.accentGreen)
                }
            }

            // Reciter Label
            Text(
                text = appText("qari", selectedReciter.name),
                style = MaterialTheme.typography.labelSmall,
                color = colors.accentGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 2.dp)
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d", mins, secs)
}

@Composable
fun AyahListView(
    surahId: Int,
    ayahs: List<Ayah>,
    activeAyahNumber: Int?,
    arabicFontSize: Float,
    translationFontSize: Float,
    readingLayout: ReadingLayout,
    listState: LazyListState,
    selectedLanguage: String,
    bookmarkedAyahs: Set<AyahBookmark>,
    colors: ThemeColors,
    onAyahClick: (Ayah) -> Unit,
    onTafsirClick: (Ayah) -> Unit,
    onToggleAyahBookmark: (Ayah) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().background(colors.bg)
    ) {
        itemsIndexed(ayahs, key = { _, ayah -> ayah.number }) { _, ayah ->
            val isActive = ayah.number == activeAyahNumber
            AyahCard(
                surahId = surahId,
                ayah = ayah,
                isActive = isActive,
                arabicFontSize = arabicFontSize,
                translationFontSize = translationFontSize,
                readingLayout = readingLayout,
                selectedLanguage = selectedLanguage,
                isBookmarked = AyahBookmark(surahId, ayah.number) in bookmarkedAyahs,
                colors = colors,
                onCardClick = { onAyahClick(ayah) },
                onTafsirClick = { onTafsirClick(ayah) },
                onToggleBookmark = { onToggleAyahBookmark(ayah) }
            )
        }
    }
}

@Composable
fun AyahCard(
    surahId: Int,
    ayah: Ayah,
    isActive: Boolean,
    arabicFontSize: Float,
    translationFontSize: Float,
    readingLayout: ReadingLayout,
    selectedLanguage: String,
    isBookmarked: Boolean,
    colors: ThemeColors,
    onCardClick: () -> Unit,
    onTafsirClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    val cardBg by animateColorAsState(
        targetValue = if (isActive) colors.gold.copy(alpha = 0.16f) else colors.cardBg,
        label = "ayahHighlight"
    )
    val context = LocalContext.current
    val copiedMessage = appText("copyAyah")
    val translationText = if (selectedLanguage == "amharic") ayah.amharic else ayah.english
    val cleanArabic = cleanSeparatedAyahText(surahId, ayah.number, ayah.arabic)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) colors.gold else colors.topBarBg
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InlineVerseNumberCircle(number = ayah.number, isActive = isActive, colors = colors)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = appText("bookmarkAyah"),
                        tint = if (isBookmarked) colors.gold else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onTafsirClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = appText("tafsiir"), tint = colors.accentGreen, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Ayah", "$cleanArabic\n\n$translationText"))
                            Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = appText("copy"), tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = cleanArabic,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = arabicFontSize.sp,
                    lineHeight = (arabicFontSize * 1.6f).sp,
                    fontFamily = FontFamily.Serif
                ),
                textAlign = TextAlign.Right,
                color = if (isActive) colors.gold else colors.textPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            if (readingLayout != ReadingLayout.ARABIC_ONLY) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translationText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = translationFontSize.sp,
                        lineHeight = (translationFontSize * 1.4f).sp
                    ),
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun SettingsCustomizerDialog(
    viewModel: QuranViewModel,
    forceOfflineMode: Boolean,
    autoScrollCentering: Boolean,
    arabicFontSize: Float,
    translationFontSize: Float,
    readingLayout: ReadingLayout,
    customSurahAudioUris: Map<Int, String>,
    selectedLanguage: String,
    selectedReciter: Reciter,
    appLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    textDownloadProgress: com.example.viewmodel.OfflineDownloadProgress,
    audioDownloadProgress: com.example.viewmodel.OfflineDownloadProgress,
    audioDownloadRequested: Boolean,
    appTheme: AppTheme,
    colors: ThemeColors,
    allSurahs: List<SurahHeader>,
    currentSurahId: Int,
    onDismiss: () -> Unit
) {
    var selectedAudioSurahId by remember(allSurahs, currentSurahId) {
        mutableIntStateOf(currentSurahId)
    }
    var showAudioSurahMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audioSavedMessage = appText("audioSaved")
    val audioPermissionMessage = appText("audioPermission")
    val audioOpenFailedMessage = appText("audioOpenFailed")
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.setCustomSurahAudio(selectedAudioSurahId, uri.toString())
                Toast.makeText(context, audioSavedMessage, Toast.LENGTH_SHORT).show()
            } catch (exception: SecurityException) {
                Toast.makeText(context, audioPermissionMessage, Toast.LENGTH_LONG).show()
            } catch (exception: IllegalArgumentException) {
                Toast.makeText(context, audioOpenFailedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(appText("quranSettings"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)

                LanguagePicker(
                    language = appLanguage,
                    label = appText("language"),
                    onLanguageSelected = onLanguageSelected
                )

                Text(
                    appText("offlineText", textDownloadProgress.completed, textDownloadProgress.total),
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    appText("internetTextDownload"),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                LinearProgressIndicator(
                    progress = { textDownloadProgress.completed.toFloat() / textDownloadProgress.total },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.accentGreen
                )
                if (textDownloadProgress.waitingForNetwork) {
                    Text(appText("waitingInternet"), color = colors.textSecondary)
                }
                if (audioDownloadProgress.waitingForNetwork) {
                    Text(appText("waitingWifi"), color = colors.textSecondary)
                }
                if (textDownloadProgress.failed > 0 || audioDownloadProgress.failed > 0) {
                    Text(
                        appText("downloadFailures", textDownloadProgress.failed + audioDownloadProgress.failed),
                        color = colors.textSecondary
                    )
                }
                Text(
                    appText("offlineAudio", audioDownloadProgress.completed, audioDownloadProgress.total),
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    appText("wifiAudioDownload"),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                LinearProgressIndicator(
                    progress = { audioDownloadProgress.completed.toFloat() / audioDownloadProgress.total },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.accentGreen
                )
                TextButton(
                    onClick = viewModel::retryOfflineDownloads,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(appText("retryTextDownloads"))
                }
                TextButton(
                    onClick = viewModel::downloadYasserAudioOverWifi,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(appText(if (audioDownloadRequested) "resumeAudioDownloads" else "downloadAudio"))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Mode Selector
                Text(appText("colorTheme"), style = MaterialTheme.typography.labelMedium, color = colors.accentGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        AppTheme.EMERALD_GREEN to appText("darkEmerald"),
                        AppTheme.DARK_BLACK to appText("pureBlack"),
                        AppTheme.LIGHT to appText("lightMode")
                    ).forEach { (themeOption, label) ->
                        val isSelected = appTheme == themeOption
                        Button(
                            onClick = { viewModel.setAppTheme(themeOption) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.accentGreen else colors.topBarBg
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Developer Credit Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.topBarBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Quran Karim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(appText("creatorCredit"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = colors.accentGreen)
                        CreatorContactLink(color = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(appText("fontSizes"), style = MaterialTheme.typography.labelMedium, color = colors.accentGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(appText("quranArabic"), color = colors.textPrimary, fontWeight = FontWeight.Medium)
                    Text("${arabicFontSize.toInt()} sp", color = colors.textSecondary)
                }
                Slider(
                    value = arabicFontSize,
                    onValueChange = viewModel::setArabicFontSize,
                    valueRange = 20f..44f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = colors.accentGreen, activeTrackColor = colors.accentGreen)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(appText("translation"), color = colors.textPrimary, fontWeight = FontWeight.Medium)
                    Text("${translationFontSize.toInt()} sp", color = colors.textSecondary)
                }
                Slider(
                    value = translationFontSize,
                    onValueChange = viewModel::setTranslationFontSize,
                    valueRange = 12f..26f,
                    steps = 13,
                    colors = SliderDefaults.colors(thumbColor = colors.accentGreen, activeTrackColor = colors.accentGreen)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Qari / Reciter Selector
                Text(appText("selectReciter"), style = MaterialTheme.typography.labelMedium, color = colors.accentGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    viewModel.availableReciters.forEach { reciter ->
                        val isSelected = reciter.id == selectedReciter.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setSelectedReciter(reciter) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) colors.accentGreen.copy(alpha = 0.2f) else colors.topBarBg
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.accentGreen else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(reciter.name, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accentGreen)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(appText("audioUploads"), style = MaterialTheme.typography.labelMedium, color = colors.accentGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { showAudioSurahMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedSurah = allSurahs.firstOrNull { it.id == selectedAudioSurahId }
                        Text(
                            selectedSurah?.let { "${it.id}. ${it.englishName}" } ?: appText("selectSurah"),
                            color = colors.textPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showAudioSurahMenu,
                        onDismissRequest = { showAudioSurahMenu = false },
                        modifier = Modifier.heightIn(max = 320.dp)
                    ) {
                        allSurahs.forEach { surah ->
                            DropdownMenuItem(
                                text = { Text("${surah.id}. ${surah.englishName}") },
                                onClick = {
                                    selectedAudioSurahId = surah.id
                                    showAudioSurahMenu = false
                                }
                            )
                        }
                    }
                }
                val selectedAudioSurah = allSurahs.firstOrNull { it.id == selectedAudioSurahId }
                val selectedAudioUri = customSurahAudioUris[selectedAudioSurahId]
                Text(
                    text = if (selectedAudioUri != null) {
                        appText("customAudioSet", selectedAudioSurah?.englishName ?: selectedAudioSurahId.toString())
                    } else {
                        appText("noCustomAudio")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Button(
                    onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGreen)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(appText("chooseAudio"))
                }
                if (selectedAudioUri != null) {
                    TextButton(
                        onClick = {
                            try {
                                context.contentResolver.releasePersistableUriPermission(
                                    Uri.parse(selectedAudioUri),
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (_: SecurityException) {
                                // The persisted grant may already have been revoked by the document provider.
                            } catch (_: IllegalArgumentException) {
                                // The stored URI may no longer be available from its document provider.
                            }
                            viewModel.setCustomSurahAudio(selectedAudioSurahId, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(appText("removeAudio"), color = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reading Mode Selector
                Text(appText("readingMode"), style = MaterialTheme.typography.labelMedium, color = colors.accentGreen, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        ReadingLayout.ARABIC_ONLY to appText("fullPageQuran"),
                        ReadingLayout.STACKED to appText("stacked"),
                        ReadingLayout.PARALLEL to appText("parallel"),
                        ReadingLayout.AUDIO to appText("audio")
                    ).forEach { (layout, label) ->
                        val isSel = layout == readingLayout
                        Button(
                            onClick = { viewModel.setReadingLayout(layout) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) colors.accentGreen else colors.topBarBg
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGreen)
                ) {
                    Text(appText("closeApply"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TafsirDialog(
    tafsirData: TafsirData?,
    isLoading: Boolean,
    isSavedForOffline: Boolean,
    selectedTafsirType: String,
    colors: ThemeColors,
    onSelectTafsirType: (String) -> Unit,
    onSaveForOffline: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(appText("authenticTafsir"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.accentGreen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tafsirData != null && !isLoading && !isSavedForOffline) {
                            IconButton(onClick = onSaveForOffline) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = appText("saveTafsirOffline"),
                                    tint = colors.accentGreen
                                )
                            }
                        } else if (isSavedForOffline) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = appText("tafsirSavedOffline"),
                                tint = colors.accentGreen,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = appText("close"), tint = colors.textPrimary)
                    }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ibnkathir" to appText("ibnKathir"), "saadi" to appText("asSaadi")).forEach { (type, label) ->
                        val isSel = selectedTafsirType == type
                        Button(
                            onClick = { onSelectTafsirType(type) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) colors.accentGreen else colors.topBarBg
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accentGreen)
                    }
                } else if (tafsirData != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(tafsirData.tafsirName, style = MaterialTheme.typography.titleMedium, color = colors.accentGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tafsirData.authorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(tafsirData.tafsirText, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, lineHeight = 26.sp)
                    }
                }
            }
        }
    }
}
