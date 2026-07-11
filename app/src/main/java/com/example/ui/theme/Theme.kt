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

private val DarkColorScheme =
  darkColorScheme(
    primary = ZallahGreen,
    secondary = Slate200,
    tertiary = ZallahGold,
    background = Slate900,
    surface = Slate800,
    onPrimary = Color.White,
    onSecondary = Slate900,
    onTertiary = Slate900,
    onBackground = Slate100,
    onSurface = Slate100,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ZallahGreen,
    secondary = Slate700,
    tertiary = ZallahGold,
    background = Slate50,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Slate900,
    onBackground = Slate900,
    onSurface = Slate900,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Set to false to enforce Zallah custom brand colors!
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
