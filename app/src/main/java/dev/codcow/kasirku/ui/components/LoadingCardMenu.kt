import androidx.compose.animation.core.*
import dev.codcow.kasirku.ui.util.rememberReduceMotion
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Skeleton in the exact shape of MenuCardPemesanan (photo, two name lines, price + add button),
 * so the grid does not jump when the real cards arrive.
 */
@Composable
fun LoadingMenuCard() {
    // One sweep shared by every block in the card, so they shine together
    val progress = if (rememberReduceMotion()) null else rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing)),
        label = "sweep"
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val shine = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

    @Composable
    fun Block(modifier: Modifier, shape: Shape = RoundedCornerShape(8.dp)) = Box(
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

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Block(Modifier.fillMaxWidth().height(108.dp), RoundedCornerShape(0.dp))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.heightIn(min = 36.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Block(Modifier.fillMaxWidth(0.9f).height(14.dp))
                Block(Modifier.fillMaxWidth(0.55f).height(14.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Block(Modifier.weight(1f).padding(end = 24.dp).height(18.dp))
                Block(Modifier.size(44.dp), CircleShape)
            }
        }
    }
}
