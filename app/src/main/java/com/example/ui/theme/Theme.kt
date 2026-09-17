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
    primary = Emerald80,
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF005238),
    onPrimaryContainer = UgandaGreenPrimaryLight,
    secondary = Gold80,
    onSecondary = Color(0xFF422D00),
    secondaryContainer = Color(0xFF5F4200),
    onSecondaryContainer = UgandaGoldContainer,
    tertiary = Ruby80,
    onTertiary = Color(0xFF690005),
    tertiaryContainer = Color(0xFF93000A),
    onTertiaryContainer = UgandaRubyContainer,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = UgandaGreenPrimary,
    onPrimary = UgandaGreenOnPrimary,
    primaryContainer = UgandaGreenPrimaryContainer,
    onPrimaryContainer = Color(0xFF002114),
    secondary = UgandaGold,
    onSecondary = UgandaOnGold,
    secondaryContainer = UgandaGoldContainer,
    onSecondaryContainer = Color(0xFF261900),
    tertiary = UgandaRuby,
    onTertiary = Color.White,
    tertiaryContainer = UgandaRubyContainer,
    onTertiaryContainer = Color(0xFF410002),
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    outline = OutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct custom fintech identity
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
