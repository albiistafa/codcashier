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

// Same drawings as the v3 canvas frames (32-unit box unless noted)
object AppIcons {
    val menu = Duo(listOf("M5 15h22a11 11 0 0 1-22 0z"), listOf("M11 11c0-2 2-2 2-4M17 11c0-2 2-2 2-4"))
    val kategori = Duo(listOf("M4 9a2 2 0 0 1 2-2h6l3 3h11a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2z"))
    val subkategori = Duo(
        listOf("M6 5h6a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z", "M20 19h6a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2h-6a2 2 0 0 1-2-2v-4a2 2 0 0 1 2-2z"),
        listOf("M9 13v10h9")
    )
    val deposit = Duo(listOf("M8 8h16a4 4 0 0 1 4 4v9a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4v-9a4 4 0 0 1 4-4z"), listOf("M4 13h24M20 20h4"))
    val pegawai = Duo(
        listOf("M7.5 11a4.5 4.5 0 1 0 9 0a4.5 4.5 0 1 0-9 0z"),
        listOf("M4 26c1.2-4.6 4.4-7 8-7s6.8 2.4 8 7M21 7.5a4 4 0 0 1 0 7.5M23 19c2.4.8 4 2.9 4.8 6")
    )
    val transaksi = Duo(listOf("M8 4h16v24l-4-2.5-4 2.5-4-2.5L8 28z"), listOf("M12 11h8M12 16h8M12 21h4"))
    val pengeluaran = Duo(
        listOf("M9 9h14a4 4 0 0 1 4 4v8a4 4 0 0 1-4 4H9a4 4 0 0 1-4-4v-8a4 4 0 0 1 4-4z"),
        listOf("M20 17h4M5 14h22M16 3v4M13 5l3 3 3-3")
    )
    val warung = Duo(
        listOf("M6 14v12h20V14z", "M4 8l2-4h20l2 4v2a4 4 0 0 1-8 0a4 4 0 0 1-8 0a4 4 0 0 1-8 0z"),
        listOf("M13 26v-6h6v6")
    )
    val receipt = Duo(emptyList(), listOf("M6 3h12v18l-3-2-3 2-3-2-3 2z", "M9 8h6M9 12h6"), box = 24f)
    val cash = Duo(listOf("M6 6h12a3 3 0 0 1 3 3v6a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V9a3 3 0 0 1 3-3z"), listOf("M9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0-5 0"), box = 24f)
    val qr = Duo(
        listOf("M5.5 4h3a1.5 1.5 0 0 1 1.5 1.5v3a1.5 1.5 0 0 1-1.5 1.5h-3A1.5 1.5 0 0 1 4 8.5v-3A1.5 1.5 0 0 1 5.5 4z"),
        listOf("M14 4h6v6h-6zM4 14h6v6H4zM14 14h2v2M20 14v1M14 20h2M18 18h2v2"), box = 24f
    )
    val card = Duo(listOf("M6 6h12a3 3 0 0 1 3 3v7a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V9a3 3 0 0 1 3-3z"), listOf("M3 10h18M15 15h2"), box = 24f)
}
