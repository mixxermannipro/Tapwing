// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.settings.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.textservice.TextServicesManager
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import helium314.keyboard.latin.settings.createPrefKeyForBooleanSettings
import helium314.keyboard.latin.settings.findIndexOfDefaultSetting
import helium314.keyboard.latin.utils.FoldableUtils
import helium314.keyboard.settings.preferences.SwitchPreference
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.keyboard.KeyboardTheme
import helium314.keyboard.keyboard.internal.KeyboardIconsSet
import helium314.keyboard.latin.BuildConfig
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ToolbarMode
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.SearchSettingsScreen
import helium314.keyboard.settings.SettingsActivity
import helium314.keyboard.settings.SettingsCardGroup
import helium314.keyboard.settings.SettingsDestination
import helium314.keyboard.settings.SettingsWithoutKey
import helium314.keyboard.settings.preferences.Preference
import helium314.keyboard.settings.preferences.PreferenceCategory
import helium314.keyboard.settings.preferences.SettingsSwitch

@Composable
private fun observedPreferences(): SharedPreferences {
    val context = LocalContext.current
    val prefs = remember(context) { context.prefs() }
    var revision by remember(prefs) { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(prefs, owner) {
        val resumeObserver = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        owner.lifecycle.addObserver(resumeObserver)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
            owner.lifecycle.removeObserver(resumeObserver)
        }
    }
    return remember(prefs, revision) { prefs }
}

@Composable
private fun ReferencePage(title: String, onClickBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    SearchSettingsScreen(onClickBack, title, emptyList(), showSearchAction = false) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
fun UnavailablePreference(name: String, description: String, showSwitch: Boolean = true) {
    val availability = stringResource(R.string.ref_not_available)
    Preference(
        name = name,
        description = if (description == availability) availability else description + "\n" + availability,
        onClick = {}, enabled = false, checked = if (showSwitch) false else null,
    ) { if (showSwitch) SettingsSwitch(checked = false, enabled = false) }
}

@Composable
fun PlannedFeatureScreen(title: String, description: String, onClickBack: () -> Unit) {
    ReferencePage(title, onClickBack) {
        SettingsCardGroup { UnavailablePreference(title, description) }
    }
}

@Composable
fun PreferencesScreen(onClickBack: () -> Unit) {
    val prefs = observedPreferences()
    val numberRow = prefs.getBoolean(Settings.PREF_SHOW_NUMBER_ROW, Defaults.PREF_SHOW_NUMBER_ROW)
    val sound = prefs.getBoolean(Settings.PREF_SOUND_ON, Defaults.PREF_SOUND_ON)
    val vibration = prefs.getBoolean(Settings.PREF_VIBRATE_ON, Defaults.PREF_VIBRATE_ON)
    SearchSettingsScreen(
        onClickBack, stringResource(R.string.ref_preferences), emptyList(), expandedTitle = true,
        showSearchAction = false,
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            PreferenceCategory(stringResource(R.string.ref_keys))
            SettingsCardGroup {
                SettingRow(Settings.PREF_SHOW_NUMBER_ROW)
                Preference(
                    name = stringResource(R.string.ref_password_number_row),
                    description = stringResource(R.string.ref_number_row_managed),
                    enabled = false, checked = numberRow, onClick = {},
                ) { SettingsSwitch(numberRow, enabled = false) }
                SettingRow(Settings.PREF_SHOW_EMOJI_KEY)
                Preference(stringResource(R.string.ref_comma), {}, description = stringResource(R.string.ref_layout_managed), enabled = false)
                Preference(stringResource(R.string.ref_period), {}, description = stringResource(R.string.ref_layout_managed), enabled = false)
            }
            PreferenceCategory(stringResource(R.string.ref_display))
            SettingsCardGroup {
                SettingRow(SettingsWithoutKey.CUSTOM_FONT)
                SettingRow(Settings.PREF_FONT_SCALE)
                SettingRow(Settings.PREF_EMOJI_FONT_SCALE)
            }
            PreferenceCategory(stringResource(R.string.ref_layout))
            SettingsCardGroup {
                SuggestionStripPreference()
                OneHandedPreference()
            }
            PreferenceCategory(stringResource(R.string.ref_input))
            SettingsCardGroup {
                SettingRow(Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD)
                SettingRow(Settings.PREF_AUTOSPACE_AFTER_SUGGESTION)
                SettingRow(Settings.PREF_AUTOSPACE_AFTER_PUNCTUATION)
                SettingRow(Settings.PREF_ABC_AFTER_SYMBOL_SPACE)
            }
            PreferenceCategory(stringResource(R.string.ref_keypress))
            SettingsCardGroup {
                SettingRow(Settings.PREF_SOUND_ON)
                if (sound) SettingRow(Settings.PREF_KEYPRESS_SOUND_VOLUME)
                else {
                    val volume = prefs.getFloat(Settings.PREF_KEYPRESS_SOUND_VOLUME, Defaults.PREF_KEYPRESS_SOUND_VOLUME)
                    Preference(stringResource(R.string.prefs_keypress_sound_volume_settings), {}, enabled = false,
                        description = if (volume < 0) stringResource(R.string.settings_system_default) else "${(volume * 100).toInt()}%")
                }
                SettingRow(Settings.PREF_VIBRATE_ON)
                if (vibration) SettingRow(Settings.PREF_VIBRATION_DURATION_SETTINGS)
                else {
                    val duration = prefs.getInt(Settings.PREF_VIBRATION_DURATION_SETTINGS, Defaults.PREF_VIBRATION_DURATION_SETTINGS)
                    Preference(stringResource(R.string.prefs_keypress_vibration_duration_settings), {}, enabled = false,
                        description = if (duration < 0) stringResource(R.string.settings_system_default)
                            else stringResource(R.string.abbreviation_unit_milliseconds, duration.toString()))
                }
                SettingRow(Settings.PREF_POPUP_ON)
                SettingRow(Settings.PREF_KEY_LONGPRESS_TIMEOUT)
                Preference(stringResource(R.string.ref_more_options), { SettingsDestination.navigateTo(SettingsDestination.AdditionalPreferences) })
            }
        }
    }
}

@Composable
private fun SettingRow(key: String) { SettingsActivity.settingsContainer[key]?.Preference() }

@Composable
fun SuggestionStripPreference() {
    val prefs = observedPreferences()
    val enabled = Settings.readToolbarMode(prefs).let { it == ToolbarMode.EXPANDABLE || it == ToolbarMode.SUGGESTION_STRIP }
    Preference(
        name = stringResource(R.string.ref_toolbar_visible), description = stringResource(R.string.ref_toolbar_summary),
        checked = enabled,
        onClick = {
            prefs.edit { putString(Settings.PREF_TOOLBAR_MODE, if (enabled) ToolbarMode.HIDDEN.name else ToolbarMode.EXPANDABLE.name) }
            KeyboardSwitcher.getInstance().setThemeNeedsReload()
        },
    ) { SettingsSwitch(enabled) }
}

@Composable
private fun OneHandedPreference() {
    val prefs = observedPreferences()
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val folded = FoldableUtils.isFolded
    val split = Settings.readSplitKeyboardEnabled(prefs, landscape, folded)
    val key = createPrefKeyForBooleanSettings(Settings.PREF_ONE_HANDED_MODE_PREFIX, findIndexOfDefaultSetting(landscape, split, folded), 3)
    SwitchPreference(
        name = stringResource(R.string.ref_one_handed), key = key, default = Defaults.PREF_ONE_HANDED_MODE,
        onCheckedChange = { KeyboardSwitcher.getInstance().setThemeNeedsReload() },
    )
}

@Composable
private fun AutoCorrectionPreference() {
    val prefs = observedPreferences()
    val enabled = prefs.getBoolean(Settings.PREF_AUTO_CORRECTION, Defaults.PREF_AUTO_CORRECTION)
    Preference(
        stringResource(R.string.autocorrect), checked = enabled,
        description = stringResource(R.string.ref_auto_correction_summary),
        onClick = { prefs.edit { putBoolean(Settings.PREF_AUTO_CORRECTION, !enabled) } },
    ) {
        IconButton(onClick = { SettingsDestination.navigateTo(SettingsDestination.CorrectionDetails) }) {
            Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.ref_auto_correction_options), Modifier.rotate(180f))
        }
        VerticalDivider(Modifier.height(32.dp))
        SettingsSwitch(enabled)
    }
}

@Composable
fun TextCorrectionScreen(onClickBack: () -> Unit) {
    observedPreferences() // Also refreshes Android-owned status after returning from settings.
    val context = LocalContext.current
    val spellcheckEnabled = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(TextServicesManager::class.java)?.isSpellCheckerEnabled ?: false
        } else {
            // The public TextServicesManager getter was added in Android 12.
            android.provider.Settings.Secure.getInt(context.contentResolver, "spell_checker_enabled", 0) == 1
        }
    }.getOrDefault(false)
    ReferencePage(stringResource(R.string.ref_correction), onClickBack) {
        Column {
            PreferenceCategory(stringResource(R.string.ref_automatic_corrections))
            SettingsCardGroup {
                AutoCorrectionPreference()
                SettingRow(Settings.PREF_AUTO_CAP)
            }
            PreferenceCategory(stringResource(R.string.ref_spelling))
            SettingsCardGroup {
                Preference(
                    stringResource(R.string.ref_system_spellcheck),
                    onClick = {
                        runCatching {
                            // AOSP exposes this page without a public Settings action constant.
                            context.startActivity(Intent(Intent.ACTION_MAIN)
                                .setClassName("com.android.settings", "com.android.settings.Settings\$SpellCheckersSettingsActivity")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }.recoverCatching {
                            context.startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }.onFailure {
                            Toast.makeText(context, R.string.ref_no_system_settings, Toast.LENGTH_LONG).show()
                        }
                    },
                    description = stringResource(R.string.ref_spellcheck_summary),
                )
                Preference(
                    stringResource(R.string.ref_spelling), {},
                    description = stringResource(R.string.ref_system_spellcheck_status),
                    checked = spellcheckEnabled, enabled = false,
                ) { SettingsSwitch(spellcheckEnabled, enabled = false) }
            }
            PreferenceCategory(stringResource(R.string.ref_suggestions))
            SettingsCardGroup {
                SettingRow(Settings.PREF_BLOCK_POTENTIALLY_OFFENSIVE)
                SuggestionStripPreference()
                SettingRow(Settings.PREF_SHOW_SUGGESTIONS)
                SettingRow(Settings.PREF_BIGRAM_PREDICTIONS)
                UnavailablePreference(stringResource(R.string.ref_contacts), stringResource(R.string.ref_contacts_summary))
                UnavailablePreference(stringResource(R.string.ref_smart_replies), stringResource(R.string.ref_ai_summary), showSwitch = false)
                Preference(stringResource(R.string.ref_more_options), { SettingsDestination.navigateTo(SettingsDestination.CorrectionDetails) })
            }
        }
    }
}

@Composable
fun ClipboardSettingsScreen(onClickBack: () -> Unit) {
    val prefs = observedPreferences()
    val history = prefs.getBoolean(Settings.PREF_ENABLE_CLIPBOARD_HISTORY, Defaults.PREF_ENABLE_CLIPBOARD_HISTORY)
    val files = prefs.getBoolean(Settings.PREF_CLIPBOARD_USE_FILES, Defaults.PREF_CLIPBOARD_USE_FILES)
    ReferencePage(stringResource(R.string.ref_clipboard), onClickBack) {
        SettingsCardGroup {
            SettingRow(Settings.PREF_SUGGEST_CLIPBOARD_CONTENT)
            UnavailablePreference(stringResource(R.string.ref_clipboard_details), stringResource(R.string.ref_not_available))
            UnavailablePreference(stringResource(R.string.ref_clipboard_screenshots), stringResource(R.string.ref_not_available))
            SettingRow(Settings.PREF_ENABLE_CLIPBOARD_HISTORY)
            SettingRow(Settings.PREF_CLIPBOARD_HISTORY_RETENTION_TIME)
        }
        if (history) SettingsCardGroup {
            SettingRow(Settings.PREF_CLIPBOARD_HISTORY_PINNED_FIRST)
            SettingRow(Settings.PREF_CLIPBOARD_USE_FILES)
            if (files) SettingRow(Settings.PREF_CLIPBOARD_FILES_SIZE_LIMIT)
        }
        Text(stringResource(R.string.ref_clipboard_privacy), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmojiSettingsScreen(onClickBack: () -> Unit) {
    ReferencePage(stringResource(R.string.ref_emoji), onClickBack) {
        SettingsCardGroup {
            SettingRow(Settings.PREF_SHOW_EMOJI_KEY)
            SettingRow(Settings.PREF_SHOW_EMOJI_DESCRIPTIONS)
            SettingRow(Settings.PREF_EMOJI_FONT_SCALE)
            SettingRow(SettingsWithoutKey.CUSTOM_EMOJI_FONT)
        }
        SettingsCardGroup { UnavailablePreference(stringResource(R.string.ref_emoji), stringResource(R.string.ref_media_summary)) }
    }
}

@Composable
fun AdvancedSettingsScreen(onClickBack: () -> Unit) {
    ReferencePage(stringResource(R.string.ref_advanced), onClickBack) {
        Column {
            PreferenceCategory(stringResource(R.string.ref_functions))
            SettingsCardGroup {
                UnavailablePreference(stringResource(R.string.ref_ai_tools), stringResource(R.string.ref_ai_summary))
                UnavailablePreference(stringResource(R.string.ref_all_keyboards), stringResource(R.string.ref_passwords_protected))
                UnavailablePreference(stringResource(R.string.ref_local_dictation), stringResource(R.string.ref_voice_summary))
                RoundedKeysPreference()
                UnavailablePreference(stringResource(R.string.ref_frequent_emoji), stringResource(R.string.ref_not_available))
            }
        }
        SettingsCardGroup {
            Preference(stringResource(R.string.ref_more_options), { SettingsDestination.navigateTo(SettingsDestination.AdvancedDetails) })
        }
        Text("Tapwing ${BuildConfig.VERSION_NAME}", Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RoundedKeysPreference() {
    val prefs = observedPreferences()
    val rounded = prefs.getString(Settings.PREF_THEME_STYLE, Defaults.PREF_THEME_STYLE) == KeyboardTheme.STYLE_ROUNDED
    Preference(
        stringResource(R.string.ref_rounded_keys), checked = rounded,
        description = stringResource(R.string.ref_rounded_summary),
        onClick = {
            prefs.edit {
                putString(Settings.PREF_THEME_STYLE, if (rounded) KeyboardTheme.STYLE_MATERIAL else KeyboardTheme.STYLE_ROUNDED)
                if (prefs.getString(Settings.PREF_THEME_COLORS, Defaults.PREF_THEME_COLORS) == KeyboardTheme.THEME_HOLO_WHITE) remove(Settings.PREF_THEME_COLORS)
                if (prefs.getString(Settings.PREF_THEME_COLORS_NIGHT, Defaults.PREF_THEME_COLORS_NIGHT) == KeyboardTheme.THEME_HOLO_WHITE) remove(Settings.PREF_THEME_COLORS_NIGHT)
            }
            KeyboardIconsSet.needsReload = true
            KeyboardSwitcher.getInstance().setThemeNeedsReload()
        },
    ) { SettingsSwitch(rounded) }
}

@Composable
private fun InformationCard(title: String, body: String) {
    SettingsCardGroup {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PrivacySettingsScreen(onClickBack: () -> Unit) {
    ReferencePage(stringResource(R.string.ref_privacy), onClickBack) {
        InformationCard(stringResource(R.string.ref_private_typing), stringResource(R.string.ref_private_typing_summary))
        InformationCard(stringResource(R.string.ref_protected_fields), stringResource(R.string.ref_protected_fields_summary))
        InformationCard(stringResource(R.string.ref_no_telemetry), stringResource(R.string.ref_no_telemetry_summary))
    }
}

@Composable
fun UpdatesSettingsScreen(onClickBack: () -> Unit) {
    val context = LocalContext.current
    ReferencePage(stringResource(R.string.ref_updates), onClickBack) {
        InformationCard("Tapwing ${BuildConfig.VERSION_NAME}", stringResource(R.string.ref_updates_summary))
        SettingsCardGroup {
            Preference(stringResource(R.string.ref_releases), {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mixxermannipro/Tapwing/releases")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.onFailure { Toast.makeText(context, context.getString(R.string.ref_no_browser), Toast.LENGTH_LONG).show() }
            })
        }
    }
}
