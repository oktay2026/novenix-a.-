package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NovenixDarkColorScheme = darkColorScheme(
    primary = NovenixCyan,
    onPrimary = Color(0xFF002430),
    primaryContainer = Color(0xFF0A3A54),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = NovenixBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF172C54),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = NovenixEmerald,
    onTertiary = Color(0xFF00291B),
    tertiaryContainer = Color(0xFF0D3D2E),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceHighest,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1D293E),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val NovenixLightColorScheme = lightColorScheme(
    primary = Color(0xFF006D8F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F2FF),
    onPrimaryContainer = Color(0xFF002433),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = Color(0xFFE0E8F3),
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val NovenixShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NovenixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NovenixDarkColorScheme else NovenixLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NovenixTypography,
        shapes = NovenixShapes,
        content = content
    )
}
