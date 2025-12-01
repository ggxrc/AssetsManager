package com.ads.assetsmanager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// === GAMER DARK THEME - Always Dark for that Arcade Feel ===
private val GamerDarkColorScheme = darkColorScheme(
    // Primary - Neon Cyan para ações principais
    primary = NeonCyan,
    onPrimary = DarkBackground,
    primaryContainer = DarkCard,
    onPrimaryContainer = NeonCyan,
    
    // Secondary - Neon Pink para destaques
    secondary = NeonPink,
    onSecondary = DarkBackground,
    secondaryContainer = DarkCardAlt,
    onSecondaryContainer = NeonPink,
    
    // Tertiary - Neon Green para sucesso/ações positivas
    tertiary = NeonGreen,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkCard,
    onTertiaryContainer = NeonGreen,
    
    // Error - Pixel Red
    error = PixelRed,
    onError = Color.White,
    errorContainer = Color(0xFF3D0A0A),
    onErrorContainer = PixelRed,
    
    // Background & Surface - Dark theme
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    
    // Outline
    outline = NeonPurple,
    outlineVariant = Color(0xFF3D3D5C)
)

@Composable
fun AssetsManagerTheme(
    // Sempre dark theme para estilo gamer
    darkTheme: Boolean = true,
    // Desativando dynamic color para manter o tema customizado
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = GamerDarkColorScheme
    
    // Configura a status bar para combinar com o tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}