package dev.codcow.kasirku.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFEEF5D2), Color(0xFFC4DE78))
)
val Primary = Color(0xFF6D8E22)
val SecondaryBackground = Color(0xFFFFFFFF)
val Neutral = Color(0xFFEEF5D2)
val Based = Color(0xFFE5E5E5)
val Success = Color(0xFF4CAF50)
val Error = Color(0xFFFF3D30)
val Warning = Color(0xFFFFC842)
val Highlight = Color(0x33000000)

@Immutable
data class AppColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val secondarySurface: Color,
    val onSecondarySurface: Color,
    val regularSurface: Color,
    val onRegularSurface: Color,
    val actionSurface: Color,
    val onActionSurface: Color,
    val highlightSurface: Color,
    val onHighlightSurface: Color,
    val primaryGradient: Brush
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        background = Color.Unspecified,
        onBackground = Color.Unspecified,
        surface = Color.Unspecified,
        onSurface = Color.Unspecified,
        secondarySurface = Color.Unspecified,
        onSecondarySurface = Color.Unspecified,
        regularSurface = Color.Unspecified,
        onRegularSurface = Color.Unspecified,
        actionSurface = Color.Unspecified,
        onActionSurface = Color.Unspecified,
        highlightSurface = Color.Unspecified,
        onHighlightSurface = Color.Unspecified,
        primaryGradient = PrimaryGradient
    )
}

val extendedColors = AppColors(
    background = SecondaryBackground,
    onBackground = Neutral,
    surface = Primary,
    onSurface = SecondaryBackground,
    secondarySurface = Color.Unspecified,
    onSecondarySurface = Color.Unspecified,
    regularSurface = Color.Unspecified,
    onRegularSurface = Color.Unspecified,
    actionSurface = Color.Unspecified,
    onActionSurface = Color.Unspecified,
    highlightSurface = Highlight,
    onHighlightSurface = Error,
    primaryGradient = PrimaryGradient
)