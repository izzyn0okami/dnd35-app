package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DndGold,
    onPrimary = Color.Black,
    primaryContainer = DndLeatherLight,
    onPrimaryContainer = DndGoldLight,
    secondary = DndCrimsonLight,
    onSecondary = Color.White,
    secondaryContainer = DndCrimson,
    onSecondaryContainer = Color.White,
    tertiary = DndArcaneBlue,
    background = DndDarkBg,
    onBackground = Color(0xFFF3ECE0),
    surface = DndDarkSurface,
    onSurface = Color(0xFFF3ECE0),
    surfaceVariant = DndDarkCard,
    onSurfaceVariant = Color(0xFFD4C5B3),
    outline = DndDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DndGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDE68A),
    onPrimaryContainer = Color(0xFF451A03),
    secondary = DndCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEE2E2),
    onSecondaryContainer = Color(0xFF7F1D1D),
    tertiary = DndArcaneBlue,
    background = DndParchment,
    onBackground = Color(0xFF292524),
    surface = DndParchmentCard,
    onSurface = Color(0xFF292524),
    surfaceVariant = Color(0xFFE7DEC8),
    onSurfaceVariant = Color(0xFF44403C),
    outline = DndParchmentBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted medieval palette for consistent roleplaying atmosphere
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
