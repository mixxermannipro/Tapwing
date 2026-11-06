// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Settings use the graphite/blue palette from the selected reference, independent of wallpaper. */
@Composable
fun Theme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (dark) darkColorScheme(
        primary = Color(0xff8fb4fa), onPrimary = Color(0xff153d78),
        primaryContainer = Color(0xff20467f), onPrimaryContainer = Color(0xffd6e3ff),
        secondary = Color(0xffafc7f5), onSecondary = Color(0xff203554),
        secondaryContainer = Color(0xff424a61), onSecondaryContainer = Color(0xffdbe3ff),
        background = Color(0xff28282a), onBackground = Color(0xffe4e4e8),
        surface = Color(0xff28282a), onSurface = Color(0xffe4e4e8),
        onSurfaceVariant = Color(0xffc7c7cd),
        surfaceContainer = Color(0xff47474b), surfaceContainerLow = Color(0xff343436),
        surfaceContainerHigh = Color(0xff47474b), surfaceContainerHighest = Color(0xff515157),
        outline = Color(0xff96969d), outlineVariant = Color(0xff28282a),
    ) else lightColorScheme(
        primary = Color(0xff42629d), onPrimary = Color.White,
        primaryContainer = Color(0xffd7e3ff), onPrimaryContainer = Color(0xff17386e),
        secondaryContainer = Color(0xffdce4f9), onSecondaryContainer = Color(0xff203554),
        background = Color(0xfff5f5fa), surface = Color(0xfff5f5fa),
        surfaceContainer = Color(0xffe6e6ed), surfaceContainerLow = Color(0xffededf3),
        surfaceContainerHigh = Color(0xffe6e6ed), surfaceContainerHighest = Color(0xffdedee7),
        onSurface = Color(0xff25252b), onSurfaceVariant = Color(0xff55555f),
        outlineVariant = Color(0xfff5f5fa),
    )
    val type = Typography()
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            bodyLarge = type.bodyLarge.copy(fontSize = 18.sp, lineHeight = 25.sp),
            bodyMedium = type.bodyMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
            titleLarge = type.titleLarge.copy(fontSize = 24.sp),
            titleMedium = type.titleMedium.copy(fontSize = 20.sp),
            titleSmall = type.titleSmall.copy(fontSize = 14.sp),
        ),
        shapes = Shapes(
            small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(32.dp),
        ),
        content = content,
    )
}

const val previewDark = true
