package com.nexus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = AccentOnPrimary,
    secondary = SurfaceInput,
    onSecondary = TextPrimary,
    tertiary = SuccessForeground,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundWarm,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = ErrorForeground,
    errorContainer = ErrorBackground,
)

@Composable
fun NexusTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = NexusTypography,
        content = content,
    )
}
