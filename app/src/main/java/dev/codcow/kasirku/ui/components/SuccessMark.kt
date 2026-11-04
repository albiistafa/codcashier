package dev.codcow.kasirku.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.rememberReduceMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * "Paid": the circle settles with a small bounce and the tick draws itself, about 0.4 s in all
 * (Gojek plays a Lottie tick at the same moment). Lunas is seen many times a day, so this stays
 * short, never holds the buttons or the amount, and is static when animations are removed.
 */
@Composable
fun SuccessMark(modifier: Modifier = Modifier, diameter: Dp = 60.dp) {
    val still = rememberReduceMotion()
    val circle = remember { Animatable(if (still) 1f else 0.6f) }
    val tick = remember { Animatable(if (still) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (still) return@LaunchedEffect
        launch { circle.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)) }
        delay(120)
        tick.animateTo(1f, tween(durationMillis = 280, easing = FastOutSlowInEasing))
    }
    val fill = AppTheme.colors.money
    val ink = AppTheme.colors.onMoney
    Canvas(modifier.size(diameter).graphicsLayer { scaleX = circle.value; scaleY = circle.value }) {
        drawCircle(fill)
        val w = size.width
        val check = Path().apply {
            moveTo(w * 0.30f, w * 0.52f)
            lineTo(w * 0.44f, w * 0.66f)
            lineTo(w * 0.71f, w * 0.37f)
        }
        val drawn = Path()
        PathMeasure().apply { setPath(check, false) }.let { it.getSegment(0f, it.length * tick.value, drawn, true) }
        drawPath(drawn, ink, style = Stroke(width = w * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
