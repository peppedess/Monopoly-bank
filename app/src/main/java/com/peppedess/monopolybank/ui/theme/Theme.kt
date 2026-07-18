package com.peppedess.monopolybank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// ─── Palette Monopoly ───────────────────────────────────────────────
val MonopolyGreen = Color(0xFF1FB25A)       // verde tabellone
val MonopolyGreenDark = Color(0xFF0B6E3A)
val MonopolyRed = Color(0xFFED1B24)         // rosso logo
val MonopolyCream = Color(0xFFF7F3E7)       // avorio carte
val MonopolyCreamDark = Color(0xFFEDE6D2)
val MonopolyGold = Color(0xFFE0A82E)        // oro banconote 500
val MonopolyInk = Color(0xFF1A1A1A)

// Colori dei gruppi di proprietà (per i giocatori)
val PropertyColors = listOf(
    Color(0xFF955436), // Marrone  – Vicolo Corto
    Color(0xFFAAE0FA), // Celeste  – Bastioni Gran Sasso
    Color(0xFFD93A96), // Rosa     – Via Accademia
    Color(0xFFF7941D), // Arancio  – Via Verdi
    Color(0xFFED1B24), // Rosso    – Via Marco Polo
    Color(0xFFFEF200), // Giallo   – Via Roma
    Color(0xFF1FB25A), // Verde    – Corso Ateneo
    Color(0xFF0072BB), // Blu      – Parco della Vittoria
)
val PropertyNames = listOf("Marrone", "Celeste", "Rosa", "Arancio", "Rosso", "Giallo", "Verde", "Blu")

/** Colore testo leggibile sopra un colore proprietà */
fun onProperty(c: Color): Color =
    if (c == Color(0xFFAAE0FA) || c == Color(0xFFFEF200)) MonopolyInk else Color.White

private val LightScheme = lightColorScheme(
    primary = MonopolyGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB6EBC9),
    onPrimaryContainer = Color(0xFF00391A),
    secondary = MonopolyRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = MonopolyGold,
    onTertiary = MonopolyInk,
    tertiaryContainer = Color(0xFFFFE8B8),
    onTertiaryContainer = Color(0xFF3F2E00),
    background = MonopolyCream,
    onBackground = MonopolyInk,
    surface = MonopolyCream,
    onSurface = MonopolyInk,
    surfaceVariant = MonopolyCreamDark,
    onSurfaceVariant = Color(0xFF4A463A),
    surfaceContainer = Color(0xFFF0EBDB),
    surfaceContainerHigh = Color(0xFFEAE4D2),
    surfaceContainerHighest = Color(0xFFE4DDC8),
    surfaceContainerLow = Color(0xFFF4F0E2),
    outline = Color(0xFF7B7667),
    error = MonopolyRed
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF7BDCA0),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF0B6E3A),
    onPrimaryContainer = Color(0xFFB6EBC9),
    secondary = Color(0xFFFFB4AB),
    onSecondary = Color(0xFF690005),
    secondaryContainer = Color(0xFF93000A),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFF2C55C),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = Color(0xFFFFE8B8),
    background = Color(0xFF15140F),
    onBackground = Color(0xFFE7E2D4),
    surface = Color(0xFF15140F),
    onSurface = Color(0xFFE7E2D4),
    surfaceVariant = Color(0xFF2A2921),
    onSurfaceVariant = Color(0xFFCCC6B5),
    surfaceContainer = Color(0xFF211F18),
    surfaceContainerHigh = Color(0xFF2B2A21),
    surfaceContainerHighest = Color(0xFF36342A),
    surfaceContainerLow = Color(0xFF1C1B14),
    outline = Color(0xFF959081)
)

val MonopolyTypography = Typography().let { t ->
    t.copy(
        displayMedium = t.displayMedium.copy(fontWeight = FontWeight.Black),
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.Bold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonopolyBankTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialExpressiveTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = MonopolyTypography,
        content = content
    )
}
