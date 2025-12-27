package com.burhan2855.borctakip.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.burhan2855.borctakip.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    onLanguageChange: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val languages = listOf(
        "tr" to "Türkçe",
        "en" to "English",
        "ru" to "Русский",
        "it" to "Italiano",
        "fr" to "Français",
        "de" to "Deutsch",
        "zh" to "中文",
        "ja" to "日本語"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.dil)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            items(languages) { (langCode, langName) ->
                ListItem(
                    headlineContent = { Text(langName) },
                    modifier = Modifier.clickable { 
                        onLanguageChange(langCode)
                        onNavigateUp()
                    }
                )
            }
        }
    }
}
