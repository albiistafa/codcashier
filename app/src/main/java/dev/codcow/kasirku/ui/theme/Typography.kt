package dev.codcow.kasirku.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.R

private val PoppinsFamily = FontFamily(
    Font(R.font.font_poppins_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.font_poppins_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.font_poppins_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.font_poppins_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.font_poppins_light, FontWeight.Light, FontStyle.Normal)
)

@Immutable
data class AppTypography(
    val display: TextStyle,
    val displayMedium: TextStyle,
    val displaySemibold: TextStyle,
    val displayBold: TextStyle,
    val heading1: TextStyle,
    val heading1Medium: TextStyle,
    val heading1Semibold: TextStyle,
    val heading1Bold: TextStyle,
    val heading2: TextStyle,
    val heading2Medium: TextStyle,
    val heading2Semibold: TextStyle,
    val heading2Bold: TextStyle,
    val heading3: TextStyle,
    val heading3Medium: TextStyle,
    val heading3Semibold: TextStyle,
    val heading3Bold: TextStyle,
    val heading4: TextStyle,
    val heading4Medium: TextStyle,
    val heading4Semibold: TextStyle,
    val heading4Bold: TextStyle,
    val heading5: TextStyle,
    val heading5Medium: TextStyle,
    val heading5Semibold: TextStyle,
    val heading5Bold: TextStyle,
    val heading6: TextStyle,
    val heading6Medium: TextStyle,
    val heading6Semibold: TextStyle,
    val heading6Bold: TextStyle,
    val label: TextStyle,
    val labelMedium: TextStyle,
    val labelSemibold: TextStyle,
    val labelBold: TextStyle,
    val paragraph1: TextStyle,
    val paragraph1Medium: TextStyle,
    val paragraph1Semibold: TextStyle,
    val paragraph1Bold: TextStyle,
    val paragraph2: TextStyle,
    val paragraph2Medium: TextStyle,
    val paragraph2Semibold: TextStyle,
    val paragraph2Bold: TextStyle
)

val LocalAppTypography = staticCompositionLocalOf {
    AppTypography(
        display = TextStyle.Default,
        displayMedium = TextStyle.Default,
        displaySemibold = TextStyle.Default,
        displayBold = TextStyle.Default,
        heading1 = TextStyle.Default,
        heading1Medium = TextStyle.Default,
        heading1Semibold = TextStyle.Default,
        heading1Bold = TextStyle.Default,
        heading2 = TextStyle.Default,
        heading2Medium = TextStyle.Default,
        heading2Semibold = TextStyle.Default,
        heading2Bold = TextStyle.Default,
        heading3 = TextStyle.Default,
        heading3Medium = TextStyle.Default,
        heading3Semibold = TextStyle.Default,
        heading3Bold = TextStyle.Default,
        heading4 = TextStyle.Default,
        heading4Medium = TextStyle.Default,
        heading4Semibold = TextStyle.Default,
        heading4Bold = TextStyle.Default,
        heading5 = TextStyle.Default,
        heading5Medium = TextStyle.Default,
        heading5Semibold = TextStyle.Default,
        heading5Bold = TextStyle.Default,
        heading6 = TextStyle.Default,
        heading6Medium = TextStyle.Default,
        heading6Semibold = TextStyle.Default,
        heading6Bold = TextStyle.Default,
        label = TextStyle.Default,
        labelMedium = TextStyle.Default,
        labelSemibold = TextStyle.Default,
        labelBold = TextStyle.Default,
        paragraph1 = TextStyle.Default,
        paragraph1Medium = TextStyle.Default,
        paragraph1Semibold = TextStyle.Default,
        paragraph1Bold = TextStyle.Default,
        paragraph2 = TextStyle.Default,
        paragraph2Medium = TextStyle.Default,
        paragraph2Semibold = TextStyle.Default,
        paragraph2Bold = TextStyle.Default
    )
}

val extendedTypography = AppTypography (
    display = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 60.sp,
        fontWeight = FontWeight.Normal
    ),
    displayMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 60.sp,
        fontWeight = FontWeight.Medium
    ),
    displaySemibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 60.sp,
        fontWeight = FontWeight.SemiBold
    ),
    displayBold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 60.sp,
        fontWeight = FontWeight.Bold
    ),
    heading1 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.Normal
    ),
    heading1Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.Medium
    ),
    heading1Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.SemiBold
    ),
    heading1Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold
    ),
    heading2 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal
    ),
    heading2Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Medium
    ),
    heading2Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.SemiBold
    ),
    heading2Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    heading3 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Normal
    ),
    heading3Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Medium
    ),
    heading3Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold
    ),
    heading3Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold
    ),
    heading4 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    heading4Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    heading4Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    heading4Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    heading5 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    heading5Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    heading5Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    heading5Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    heading6 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal
    ),
    heading6Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    ),
    heading6Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    ),
    heading6Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    label = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSemibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelBold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    paragraph1 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    paragraph1Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    ),
    paragraph1Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    ),
    paragraph1Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    ),
    paragraph2 = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ),
    paragraph2Medium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    ),
    paragraph2Semibold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    ),
    paragraph2Bold = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
)