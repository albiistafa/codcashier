package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import dev.codcow.kasirku.ui.theme.AppTheme

/** Two-tone icon from SVG path data: [fill] paths get the soft fill, every path gets the stroke. */
class Duo(val fill: List<String>, val line: List<String> = emptyList(), val box: Float = 32f)

@Composable
fun DuoIcon(
    icon: Duo,
    size: Dp,
    fill: Color = AppTheme.colors.money,
    stroke: Color = MaterialTheme.colorScheme.primary
) {
    val fills = remember(icon) { icon.fill.map { PathParser().parsePathString(it).toPath() } }
    val lines = remember(icon) { icon.line.map { PathParser().parsePathString(it).toPath() } }
    Canvas(Modifier.size(size)) {
        scale(this.size.width / icon.box, pivot = Offset.Zero) {
            val style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            fills.forEach { drawPath(it, fill); drawPath(it, stroke, style = style) }
            lines.forEach { drawPath(it, stroke, style = style) }
        }
    }
}

// Same drawings as the v3 canvas nav (24-unit box)
object NavIcons {
    val home = Duo(listOf("M3.5 10.5L12 4l8.5 6.5V20a1 1 0 0 1-1 1h-15a1 1 0 0 1-1-1z"), listOf("M9.5 21v-5h5v5"), box = 24f)
    val kasir = Duo(listOf("M6 3h12v18l-3-2-3 2-3-2-3 2z"), listOf("M9 8h6M9 12h6"), box = 24f)
    val transaksi = Duo(
        listOf("M7 3h10a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3H7a3 3 0 0 1-3-3V6a3 3 0 0 1 3-3z"),
        listOf("M8 8h8M8 12h8M8 16h5"), box = 24f
    )
    val rekap = Duo(
        listOf(
            "M5.5 11h1A1.5 1.5 0 0 1 8 12.5v6A1.5 1.5 0 0 1 6.5 20h-1A1.5 1.5 0 0 1 4 18.5v-6A1.5 1.5 0 0 1 5.5 11z",
            "M11.5 5h1A1.5 1.5 0 0 1 14 6.5v12a1.5 1.5 0 0 1-1.5 1.5h-1a1.5 1.5 0 0 1-1.5-1.5v-12A1.5 1.5 0 0 1 11.5 5z",
            "M17.5 14h1a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1-1.5 1.5h-1a1.5 1.5 0 0 1-1.5-1.5v-3a1.5 1.5 0 0 1 1.5-1.5z"
        ),
        box = 24f
    )
}
