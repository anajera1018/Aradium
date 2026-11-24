package com.aradium.aradium

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Style: the app's only colors. Change these to restyle the whole app.
val White = Color(0xFFFFFFFF)     // Style: white
val Black = Color(0xFF000000)     // Style: black
val LightGrey = Color(0xFFE0E0E0) // Style: light grey (boxes in light mode)
val MidGrey = Color(0xFF9E9E9E)   // Style: middle grey (borders, hints)
val DarkGrey = Color(0xFF303030)  // Style: dark grey (boxes in dark mode)

// Style: colors used in DARK mode
private val DarkColors = darkColorScheme(
    primary = White,                  // Style: main buttons, sliders, active icons
    onPrimary = Black,                // Style: text/icons on main buttons
    primaryContainer = DarkGrey,
    onPrimaryContainer = White,
    inversePrimary = Black,
    secondary = MidGrey,
    onSecondary = Black,
    secondaryContainer = DarkGrey,    // Style: selected tab bubble in bottom bar
    onSecondaryContainer = White,
    tertiary = MidGrey,
    onTertiary = Black,
    tertiaryContainer = DarkGrey,
    onTertiaryContainer = White,
    background = Black,               // Style: page background
    onBackground = White,             // Style: page text
    surface = Black,                  // Style: surface background
    onSurface = White,                // Style: normal text and icons
    surfaceVariant = DarkGrey,        // Style: empty cover boxes, mini player
    onSurfaceVariant = LightGrey,     // Style: secondary text and icons
    surfaceTint = Black,
    inverseSurface = White,
    inverseOnSurface = Black,
    error = LightGrey,                // Style: error text
    onError = Black,
    errorContainer = DarkGrey,
    onErrorContainer = White,
    outline = MidGrey,                // Style: borders of text fields and outlined buttons
    outlineVariant = DarkGrey,        // Style: divider lines
    scrim = Black,
    surfaceBright = DarkGrey,
    surfaceDim = Black,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Color(0xFF101010),
    surfaceContainer = Color(0xFF181818),     // Style: bottom navigation bar
    surfaceContainerHigh = Color(0xFF202020), // Style: dialogs and menus
    surfaceContainerHighest = DarkGrey        // Style: cards
)

// Style: colors used in LIGHT mode
private val LightColors = lightColorScheme(
    primary = Black,                  // Style: main buttons, sliders, active icons
    onPrimary = White,                // Style: text/icons on main buttons
    primaryContainer = LightGrey,
    onPrimaryContainer = Black,
    inversePrimary = White,
    secondary = MidGrey,
    onSecondary = White,
    secondaryContainer = LightGrey,   // Style: selected tab bubble in bottom bar
    onSecondaryContainer = Black,
    tertiary = MidGrey,
    onTertiary = White,
    tertiaryContainer = LightGrey,
    onTertiaryContainer = Black,
    background = White,               // Style: page background
    onBackground = Black,             // Style: page text
    surface = White,                  // Style: surface background
    onSurface = Black,                // Style: normal text and icons
    surfaceVariant = LightGrey,       // Style: empty cover boxes, mini player
    onSurfaceVariant = DarkGrey,      // Style: secondary text and icons
    surfaceTint = White,
    inverseSurface = Black,
    inverseOnSurface = White,
    error = DarkGrey,                 // Style: error text
    onError = White,
    errorContainer = LightGrey,
    onErrorContainer = Black,
    outline = MidGrey,                // Style: borders of text fields and outlined buttons
    outlineVariant = LightGrey,       // Style: divider lines
    scrim = Black,
    surfaceBright = White,
    surfaceDim = LightGrey,
    surfaceContainerLowest = White,
    surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainer = Color(0xFFF0F0F0),     // Style: bottom navigation bar
    surfaceContainerHigh = Color(0xFFEBEBEB), // Style: dialogs and menus
    surfaceContainerHighest = LightGrey       // Style: cards
)

// Style: corner roundness for cards, dialogs, menus, text fields. 0.dp = square box.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

// Style: fonts and text sizes. Typography() = Android default.
private val AppTypography = Typography()

@Composable
fun AradiumTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors, // Style: picks dark or light colors
        shapes = AppShapes,         // Style: applies corner shapes
        typography = AppTypography, // Style: applies fonts
        content = content
    )
}
