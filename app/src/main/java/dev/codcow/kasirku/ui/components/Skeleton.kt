package dev.codcow.kasirku.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.util.rememberReduceMotion

/**
 * One grey block of a skeleton, with a light sweep across it. Blocks composed together start their
 * sweep on the same frame, so a whole skeleton shines as one. Still when animations are removed.
 */
@Composable
fun SkeletonBlock(modifier: Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val progress = if (rememberReduceMotion()) null else rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing)),
        label = "sweep"
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val shine = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    Box(
        modifier.clip(shape).drawWithCache {
            onDrawBehind {
                val p = progress?.value
                if (p == null) {
                    drawRect(base)
                } else {
                    val x = -size.width + p * size.width * 3
                    drawRect(Brush.linearGradient(listOf(base, shine, base), Offset(x, 0f), Offset(x + size.width, size.height)))
                }
            }
        }
    )
}
