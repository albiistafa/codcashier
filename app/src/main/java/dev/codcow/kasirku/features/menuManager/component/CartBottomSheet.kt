package dev.codcow.kasirku.features.menuManager.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

@Composable
fun AnimatedCartIndicator(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onClick: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && cartItems.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        CartIndicatorBar(
            cartItems = cartItems,
            totalPrice = totalPrice,
            onClick = onClick
        )
    }
}

@Composable
fun CartIndicatorBar(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clickable(onClick = onClick),
        color = AppTheme.colors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "${cartItems.sumOf { it.quantity }} item",
                    color = Color.White,
                    style = AppTheme.typography.paragraph1Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cartItems.joinToString(", ") { it.name },
                    color = Color.White.copy(alpha = 0.8f),
                    style = AppTheme.typography.paragraph2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatRupiah(totalPrice.toString()),
                    color = Color.White,
                    style = AppTheme.typography.paragraph1Bold
                )
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Lihat Keranjang",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AnimatedCartIndicatorPreview_Visible() {
    val sampleItems = listOf(
        CartItem(1, 1, "Soto Ayam", "12000.0", photo = ""),
        CartItem(2, 1, "Tahu Telur", "12000.0", photo = "" )
    )
    val sampleTotal = 24000.0

    AppTheme {
        // Panggil wrapper animasi dengan isVisible = true
        AnimatedCartIndicator(
            cartItems = sampleItems,
            totalPrice = sampleTotal,
            onClick = {},
            isVisible = true // Tes saat terlihat
        )
    }
}