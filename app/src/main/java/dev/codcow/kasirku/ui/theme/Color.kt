package dev.codcow.kasirku.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Everything UI reads goes through MaterialTheme.colorScheme roles.
// AppColors only holds what Material has no role for.

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF526C18),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF5D2),
    onPrimaryContainer = Color(0xFF263604),
    // Selected segments, chips and nav indicators use secondaryContainer: keep them in the leaf family
    secondaryContainer = Color(0xFFE2EAC8),
    onSecondaryContainer = Color(0xFF263604),
    background = Color.White,
    onBackground = Color(0xFF20251A),
    surface = Color.White,
    onSurface = Color(0xFF20251A),
    surfaceVariant = Color(0xFFF3F6EA),
    onSurfaceVariant = Color(0xFF455038),
    outline = Color(0xFF6D7860),
    outlineVariant = Color(0xFFD7DDCB),
    // Kunyit: unpaid / waiting, kept apart from the green "done" by lightness, not hue alone
    tertiary = Color(0xFFB7790F),
    tertiaryContainer = Color(0xFFFBEBC8),
    onTertiaryContainer = Color(0xFF4A3000),
    error = Color(0xFFB3261E),
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC6E889),
    onPrimary = Color(0xFF253700),
    primaryContainer = Color(0xFF394F0C),
    onPrimaryContainer = Color(0xFFE0F5BA),
    secondaryContainer = Color(0xFF3A4A28),
    onSecondaryContainer = Color(0xFFDCE9C4),
    background = Color(0xFF12170E),
    onBackground = Color(0xFFE5EBDD),
    surface = Color(0xFF1B2316),
    onSurface = Color(0xFFE5EBDD),
    surfaceVariant = Color(0xFF2A3524),
    onSurfaceVariant = Color(0xFFC3D0B7),
    outline = Color(0xFF89977E),
    outlineVariant = Color(0xFF424C3A),
    tertiary = Color(0xFFF2BE5C),
    tertiaryContainer = Color(0xFF5C4100),
    onTertiaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Immutable
data class AppColors(
    /** Full-bleed brand backdrop (splash, login). */
    val primaryGradient: Brush,
    /** Money summary cards: stays deep green in both themes, so content sits on [onHero]. */
    val hero: List<Color>,
    val onHero: Color,
    /** Paid / settled status. */
    val success: Color,
    /** Kunyit: only for the action that starts taking money (Mulai jualan). */
    val money: Color = Color(0xFFF4B740),
    val onMoney: Color = Color(0xFF2B1D00)
)

private val HeroGradient = listOf(Color(0xFF526C18), Color(0xFF394F0C), Color(0xFF526C18))

val extendedColors = AppColors(
    primaryGradient = Brush.linearGradient(listOf(Color(0xFFEEF5D2), Color(0xFFC4DE78))),
    hero = HeroGradient,
    onHero = Color.White,
    success = Color(0xFF2E7D32)
)

val darkExtendedColors = AppColors(
    primaryGradient = Brush.linearGradient(listOf(Color(0xFF263914), Color(0xFF425A1D))),
    hero = HeroGradient,
    onHero = Color.White,
    success = Color(0xFF81C784)
)

val LocalAppColors = staticCompositionLocalOf { extendedColors }
