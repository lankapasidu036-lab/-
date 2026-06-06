package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF6366F1),
    secondary = Color(0xFFA855F7),
    tertiary = Color(0xFFF43F5E),
    background = Color(0xFF0D0B14),
    surface = Color(0xFF161421),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9)
  )

private val LightColorScheme = DarkColorScheme // Keep it consistently immersive

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for immersion
  dynamicColor: Boolean = false, // Use our handcrafted luxury colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

