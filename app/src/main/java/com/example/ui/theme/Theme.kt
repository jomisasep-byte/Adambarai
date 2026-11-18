package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CyberDarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberVoid,
    primaryContainer = Color(0xFF003642),
    onPrimaryContainer = CyberCyan,
    secondary = CyberViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2B1A59),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = CyberSuccess,
    onTertiary = CyberVoid,
    tertiaryContainer = CyberSuccessContainer,
    onTertiaryContainer = CyberSuccess,
    background = CyberVoid,
    onBackground = CyberTextPrimary,
    surface = CyberNavy,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurface,
    onSurfaceVariant = CyberTextSecondary,
    surfaceContainerLowest = CyberVoid,
    surfaceContainerLow = CyberNavy,
    surfaceContainer = CyberSurface,
    surfaceContainerHigh = CyberSurfaceElevated,
    surfaceContainerHighest = Color(0xFF1D2C4D),
    outline = CyberSurfaceBorder,
    outlineVariant = Color(0xFF1A2742),
    error = CyberError,
    onError = Color.White,
    errorContainer = CyberErrorContainer,
    onErrorContainer = Color(0xFFFCA5A5),
  )

private val CyberLightColorScheme =
  lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F4FC),
    onPrimaryContainer = Color(0xFF003642),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF3B0764),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
  )

val MusicGuardShapes =
  Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
  )

@Composable
fun MusicGuardTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = MusicGuardTypography,
    shapes = MusicGuardShapes,
    content = content,
  )
}
