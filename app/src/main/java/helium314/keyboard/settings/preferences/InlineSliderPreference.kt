// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.settings.preferences

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.prefs

@Suppress("UNCHECKED_CAST")
@Composable
internal fun <T : Number> InlineSliderPreference(
    name: String,
    modifier: Modifier,
    key: String,
    description: @Composable (T) -> String,
    default: T,
    range: ClosedFloatingPointRange<Float>,
    stepSize: Int?,
    onValueChanged: (Float?) -> Unit,
    onConfirmed: (T) -> Unit,
) {
    val prefs = LocalContext.current.prefs()
    var position by remember(prefs, key, default) {
        mutableFloatStateOf(getPrefOfType(prefs, key, default).toFloat().coerceIn(range))
    }
    DisposableEffect(prefs, key, default) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changed ->
            if (changed == key || changed == null) position = getPrefOfType(prefs, key, default).toFloat().coerceIn(range)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val current = (if (default is Int) position.toInt() else position) as T
    Column(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Text(description(current), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (key == Settings.PREF_EMOJI_FONT_SCALE)
            Text("😂 😘 🙂 ❤️ 🙌 😎 🔥 🎉", fontSize = (20 * position).sp)
        Slider(
            value = position,
            onValueChange = { position = it },
            onValueChangeFinished = {
                val value = (if (default is Int) position.toInt() else position) as T
                prefs.edit {
                    if (default is Int) putInt(key, value.toInt()) else putFloat(key, value.toFloat())
                }
                onConfirmed(value)
                onValueChanged(position)
            },
            valueRange = range,
            steps = stepSize?.let { ((range.endInclusive - range.start) / it - 1).toInt().coerceAtLeast(0) } ?: 0,
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = position != default.toFloat(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (position != default.toFloat()) 1f else 0.18f)),
            onClick = {
                prefs.edit { remove(key) }
                position = default.toFloat().coerceIn(range)
                onConfirmed(default)
            },
        ) { Text(stringResource(R.string.ref_restore_default)) }
    }
}
