// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.SubtypeLocaleUtils.displayName
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.settings.SettingsCardGroup
import helium314.keyboard.settings.SettingsDestination
import helium314.keyboard.settings.preferences.Preference

@Composable
fun MainSettingsScreen(onNavigate: (String) -> Unit, onClickBack: () -> Unit) {
    BackHandler(onBack = onClickBack)
    SettingsHomeContent(
        languageSummary = SubtypeSettings.getEnabledSubtypes(true).joinToString(", ") { it.displayName() },
        onNavigate = onNavigate,
    )
}

@Composable
fun SettingsHomeContent(languageSummary: String, onNavigate: (String) -> Unit) {
    val groups = listOf(
        listOf(
            Triple(R.string.ref_languages, R.drawable.ic_settings_languages, SettingsDestination.Languages),
            Triple(R.string.ref_preferences, R.drawable.ic_settings_preferences, SettingsDestination.Preferences),
            Triple(R.string.ref_design, R.drawable.ic_settings_appearance, SettingsDestination.Appearance),
        ),
        listOf(
            Triple(R.string.ref_correction, R.drawable.ic_settings_correction, SettingsDestination.TextCorrection),
            Triple(R.string.ref_swipe, R.drawable.ic_settings_gesture, SettingsDestination.GestureTyping),
            Triple(R.string.ref_voice, R.drawable.sym_keyboard_voice_rounded, SettingsDestination.Voice),
            Triple(R.string.ref_autofill, R.drawable.ic_settings_layout, SettingsDestination.Autofill),
            Triple(R.string.ref_emoji, R.drawable.ic_emoji_smileys_emotion_rounded, SettingsDestination.Emoji),
        ),
        listOf(
            Triple(R.string.ref_clipboard, R.drawable.sym_keyboard_clipboard_rounded, SettingsDestination.Clipboard),
            Triple(R.string.ref_dictionary, R.drawable.ic_dictionary, SettingsDestination.Dictionaries),
        ),
        listOf(
            Triple(R.string.ref_privacy, R.drawable.ic_settings_about_license, SettingsDestination.Privacy),
            Triple(R.string.ref_about, R.drawable.ic_settings_about, SettingsDestination.About),
            Triple(R.string.ref_advanced, R.drawable.ic_settings_advanced, SettingsDestination.Advanced),
            Triple(R.string.ref_updates, R.drawable.ic_settings_about_version, SettingsDestination.Updates),
        ),
    )
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Text(
                stringResource(R.string.ref_settings_title),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 40.dp),
                style = MaterialTheme.typography.titleLarge,
            )
            Column(verticalArrangement = Arrangement.spacedBy(28.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                groups.forEach { group ->
                    SettingsCardGroup {
                        group.forEach { (title, icon, destination) ->
                            Preference(
                                name = stringResource(title),
                                description = if (destination == SettingsDestination.Languages) languageSummary else null,
                                icon = icon,
                                onClick = { onNavigate(destination) },
                            )
                        }
                    }
                }
            }
        }
    }
}
