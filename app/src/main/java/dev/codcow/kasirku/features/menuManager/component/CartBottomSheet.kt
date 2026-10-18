package dev.codcow.kasirku.features.menuManager.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import dev.codcow.kasirku.ui.util.rememberReduceMotion

/** Thermal-receipt tear along the top edge: the order reads as the struk it will become. */
fun receiptEdgeShape(toothWidthPx: Float, toothHeightPx: Float) = GenericShape { size, _ ->
    val teeth = (size.width / toothWidthPx).toInt().coerceAtLeast(1)
    val w = size.width / teeth
    moveTo(0f, toothHeightPx)
    repeat(teeth) { i ->
        lineTo(i * w + w / 2, 0f)
        lineTo((i + 1) * w, toothHeightPx)
    }
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

@Composable
fun AnimatedCartIndicator(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onOpen: () -> Unit,
    onPay: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val exitDistance = with(LocalDensity.current) { 12.dp.roundToPx() }
    val reduceMotion = rememberReduceMotion()
    AnimatedVisibility(
        visible = isVisible && cartItems.isNotEmpty(),
        enter = if (reduceMotion) EnterTransition.None else slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = if (reduceMotion) ExitTransition.None else slideOutVertically(targetOffsetY = { exitDistance }) + fadeOut(),
        modifier = modifier
    ) {
        CartIndicatorBar(cartItems = cartItems, totalPrice = totalPrice, onOpen = onOpen, onPay = onPay)
    }
}

/** Running struk: tap the left side to edit the order, Bayar to pay without opening it. */
@Composable
fun CartIndicatorBar(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onOpen: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val edge = with(LocalDensity.current) { receiptEdgeShape(12.dp.toPx(), 6.dp.toPx()) }
    Surface(
        shape = edge,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clickable(onClickLabel = "Lihat pesanan", onClick = onOpen),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${cartItems.sumOf { it.quantity }} item · ${cartItems.joinToString(", ") { it.name }}",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatRupiah(totalPrice.toString()),
                    style = AppTheme.typography.heading5Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = onPay,
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.heightIn(min = 52.dp)
            ) {
                Text("Bayar", style = AppTheme.typography.paragraph1Semibold, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F6EA)
@Composable
fun AnimatedCartIndicatorPreview_Visible() {
    AppTheme {
        CartIndicatorBar(
            cartItems = listOf(
                CartItem(1, 1, "Soto Ayam", "12000.0", photo = ""),
                CartItem(2, 2, "Es Teh", "4000.0", photo = "")
            ),
            totalPrice = 20000.0,
            onOpen = {},
            onPay = {}
        )
    }
}
