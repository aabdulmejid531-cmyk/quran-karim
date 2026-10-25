package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AppLanguage
import com.example.ui.screens.LocalAppLanguage
import com.example.ui.screens.QuranReaderScreen
import com.example.ui.screens.StartupOptionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTheme
import com.example.viewmodel.QuranViewModel
import com.example.viewmodel.ReadingLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QuranViewModel = viewModel()
            val appTheme by viewModel.appTheme.collectAsState()

            MyApplicationTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showStartup by rememberSaveable { mutableStateOf(true) }
                    var returnToReaderFromStartup by remember { mutableStateOf(false) }
                    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
                    val screenStateHolder = rememberSaveableStateHolder()
                    val languagePreferences = remember {
                        getSharedPreferences("app_preferences", MODE_PRIVATE)
                    }
                    var selectedLanguage by remember {
                        mutableStateOf(
                            AppLanguage.fromTag(languagePreferences.getString("app_language", "am"))
                        )
                    }

                    LaunchedEffect(selectedLanguage) {
                        viewModel.setSelectedLanguage(selectedLanguage.quranTranslationLanguage)
                    }
                    BackHandler {
                        when {
                            showStartup && returnToReaderFromStartup -> {
                                showStartup = false
                                returnToReaderFromStartup = false
                            }
                            showStartup -> showExitConfirmation = true
                            else -> showStartup = true
                        }
                    }

                    CompositionLocalProvider(
                        LocalAppLanguage provides selectedLanguage,
                        LocalLayoutDirection provides selectedLanguage.layoutDirection
                    ) {
                        if (showStartup) {
                            screenStateHolder.SaveableStateProvider("startup") {
                                StartupOptionsScreen(
                                    language = selectedLanguage,
                                    onLanguageSelected = { language ->
                                        selectedLanguage = language
                                        languagePreferences.edit().putString("app_language", language.tag).apply()
                                    },
                                    onOptionSelected = { option ->
                                        when (option) {
                                            "quran_only" -> viewModel.setReadingLayout(ReadingLayout.ARABIC_ONLY)
                                            "quran_tafsir", "quran_meaning" -> viewModel.setReadingLayout(ReadingLayout.STACKED)
                                            "quran_audio" -> viewModel.setReadingLayout(ReadingLayout.AUDIO)
                                        }
                                        returnToReaderFromStartup = false
                                        showStartup = false
                                    }
                                )
                            }
                        } else {
                            screenStateHolder.SaveableStateProvider("reader") {
                                QuranReaderScreen(
                                    viewModel = viewModel,
                                    appLanguage = selectedLanguage,
                                    onLanguageSelected = { language ->
                                        selectedLanguage = language
                                        languagePreferences.edit().putString("app_language", language.tag).apply()
                                    },
                                    onOpenStartup = {
                                        returnToReaderFromStartup = true
                                        showStartup = true
                                    }
                                )
                            }
                        }
                        if (showExitConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showExitConfirmation = false },
                                title = { Text(selectedLanguage.text("exitTitle")) },
                                text = { Text(selectedLanguage.text("exitMessage")) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showExitConfirmation = false
                                        finish()
                                    }) {
                                        Text(selectedLanguage.text("exit"))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitConfirmation = false }) {
                                        Text(selectedLanguage.text("cancel"))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
