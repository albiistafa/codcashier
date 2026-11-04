import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.components.SkeletonBlock

/**
 * Skeleton in the exact shape of MenuCardPemesanan (photo, two name lines, price + add button),
 * so the grid does not jump when the real cards arrive.
 */
@Composable
fun LoadingMenuCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        SkeletonBlock(Modifier.fillMaxWidth().height(108.dp), RoundedCornerShape(0.dp))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.heightIn(min = 36.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBlock(Modifier.fillMaxWidth(0.9f).height(14.dp))
                SkeletonBlock(Modifier.fillMaxWidth(0.55f).height(14.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(Modifier.weight(1f).padding(end = 24.dp).height(18.dp))
                SkeletonBlock(Modifier.size(44.dp), CircleShape)
            }
        }
    }
}
