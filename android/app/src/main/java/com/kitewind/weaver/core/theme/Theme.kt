package com.kitewind.weaver.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalReducedMotion = staticCompositionLocalOf { false }

private val CandyScheme = lightColorScheme(
    primary = BlossomPrimary, onPrimary = CloudWhite,
    secondary = LimeAccent, onSecondary = Ink,
    tertiary = DustyPink, onTertiary = Ink,
    background = PetalBackground, onBackground = Ink,
    surface = CloudWhite, onSurface = Ink,
    surfaceVariant = CandyMist, onSurfaceVariant = Ink,
    outline = DustyPink, error = BlossomPrimary, onError = CloudWhite
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CandyScheme, typography = AppTypography, content = content)
}
