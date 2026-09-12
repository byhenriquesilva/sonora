package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun sonoraDarkColorScheme(accent: Color = SonoraAccentEmerald) = darkColorScheme(
  primary = accent,
  onPrimary = Color.Black,
  primaryContainer = SonoraCardSelected,
  onPrimaryContainer = accent,
  secondary = SonoraTextSecondary,
  onSecondary = Color.White,
  secondaryContainer = SonoraCard,
  onSecondaryContainer = SonoraTextPrimary,
  tertiary = SonoraAccentCyan,
  onTertiary = Color.Black,
  background = SonoraBackground,
  onBackground = SonoraTextPrimary,
  surface = SonoraSecondaryBackground,
  onSurface = SonoraTextPrimary,
  surfaceVariant = SonoraCard,
  onSurfaceVariant = SonoraTextSecondary,
  error = SonoraError,
  onError = Color.White,
  outline = SonoraBorder
)

fun sonoraLightColorScheme(accent: Color = SonoraAccentEmerald) = lightColorScheme(
  primary = accent,
  onPrimary = Color.Black,
  primaryContainer = SonoraLightCardSelected,
  onPrimaryContainer = SonoraLightTextPrimary,
  secondary = SonoraLightTextSecondary,
  onSecondary = Color.White,
  secondaryContainer = SonoraLightCard,
  onSecondaryContainer = SonoraLightTextPrimary,
  tertiary = SonoraAccentCyan,
  onTertiary = Color.Black,
  background = SonoraLightBackground,
  onBackground = SonoraLightTextPrimary,
  surface = SonoraLightCard,
  onSurface = SonoraLightTextPrimary,
  surfaceVariant = SonoraLightSecondary,
  onSurfaceVariant = SonoraLightTextSecondary,
  error = SonoraError,
  onError = Color.White,
  outline = SonoraLightCardSelected
)

@Composable
fun SonoraTheme(
  darkTheme: Boolean = true,
  accentColor: Color = SonoraAccentEmerald,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) {
    sonoraDarkColorScheme(accentColor)
  } else {
    sonoraLightColorScheme(accentColor)
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  SonoraTheme(darkTheme = darkTheme, content = content)
}

