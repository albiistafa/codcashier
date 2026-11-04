package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

/** Kasirku's mark: a struk under the printer slot. Splash animates [paperFraction] and [totalScale]; Login shows it still. */
@Composable
fun StrukMark(modifier: Modifier = Modifier, paperFraction: Float = 1f, totalScale: Float = 1f) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(132.dp).height(14.dp).background(AppTheme.colors.deep, RoundedCornerShape(7.dp)))
        val teeth = with(LocalDensity.current) { tornBottom(12.dp.toPx(), 5.dp.toPx()) }
        Column(
            Modifier
                .offset(y = (-7).dp)
                .width(112.dp)
                // Reveal from the slot downward, like paper feeding out
                .graphicsLayer { clip = true; shape = RevealShape(paperFraction) }
                .clip(teeth)
                .background(AppTheme.colors.onHero)
                .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            val line = AppTheme.colors.deep.copy(alpha = 0.12f)
            listOf(0.7f, 0.9f, 0.55f).forEach { w ->
                Box(Modifier.fillMaxWidth(w).height(6.dp).background(line, RoundedCornerShape(3.dp)))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(line))
            Box(
                Modifier
                    .align(Alignment.End)
                    .fillMaxWidth(0.62f)
                    .height(8.dp)
                    .graphicsLayer { scaleX = totalScale; transformOrigin = TransformOrigin(1f, 0.5f) }
                    .background(AppTheme.colors.money, RoundedCornerShape(4.dp))
            )
        }
    }
}

/** Shows the top [fraction] of the paper. */
private class RevealShape(private val fraction: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Rectangle(Rect(0f, 0f, size.width, size.height * fraction))
}

/** Torn bottom edge of the struk. */
private fun tornBottom(toothWidthPx: Float, toothHeightPx: Float) = GenericShape { size, _ ->
    val teeth = (size.width / toothWidthPx).toInt().coerceAtLeast(1)
    val w = size.width / teeth
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - toothHeightPx)
    repeat(teeth) { i ->
        val x = size.width - i * w
        lineTo(x - w / 2, size.height)
        lineTo(x - w, size.height - toothHeightPx)
    }
    close()
}
