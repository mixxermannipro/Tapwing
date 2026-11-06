// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import helium314.keyboard.settings.preferences.PreferenceCategory

/** One outer shape, with narrow background-colored separators between its rows. */
@Composable
fun SettingsCardGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content,
    )
}

/** Retains all existing preference content while grouping each category into one card. */
@Composable
fun SettingsPreferenceGroups(settings: List<Any?>) {
    val groups = mutableListOf<Pair<Int?, MutableList<Any>>>()
    var title: Int? = null
    var keys = mutableListOf<Any>()
    for (item in settings.filterNotNull()) {
        if (item is Int) {
            if (keys.isNotEmpty()) groups.add(title to keys)
            title = item
            keys = mutableListOf()
        } else if (SettingsActivity.settingsContainer[item] != null) keys.add(item)
    }
    if (keys.isNotEmpty()) groups.add(title to keys)
    Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
        groups.forEachIndexed { index, (heading, items) ->
            if (heading != null) PreferenceCategory(stringResource(heading))
            else if (index > 0) androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 28.dp))
            SettingsCardGroup {
                items.forEach { SettingsActivity.settingsContainer[it]?.Preference() }
            }
        }
    }
}
