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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import dev.codcow.kasirku.ui.components.DuoIcon
import dev.codcow.kasirku.ui.components.NavIcons
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
    val onDeep = AppTheme.colors.onDeep
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AppTheme.colors.deep,
        shadowElevation = 10.dp,
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clickable(onClickLabel = "Lihat pesanan", onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(44.dp).background(onDeep.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    DuoIcon(NavIcons.kasir, 24.dp, fill = AppTheme.colors.money, stroke = onDeep)
                }
                Column {
                    Text(
                        text = "${cartItems.sumOf { it.quantity }} item",
                        style = AppTheme.typography.paragraph2,
                        color = onDeep.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatRupiah(totalPrice.toString()).replace("Rp ", "Rp"),
                        style = AppTheme.typography.heading5Bold,
                        color = onDeep
                    )
                }
            }
            Button(
                onClick = onPay,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.money, contentColor = AppTheme.colors.onMoney),
                modifier = Modifier.heightIn(min = 52.dp)
            ) {
                Text("Bayar", style = AppTheme.typography.paragraph1Bold, modifier = Modifier.padding(horizontal = 10.dp))
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
