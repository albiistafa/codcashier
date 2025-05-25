package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TransactionReceipt(
    cartItems: List<CartItem>,
    totalBiaya: Double,
    paymentMethod: String?,
    customerName: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight() // Ubah ke wrapContentHeight agar sesuai dengan isi
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF) // Light Green background
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Transaksi Berhasil",
                    style = AppTheme.typography.labelBold,
                    color = Color(0xFF228B22), // Green color
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!customerName.isNullOrBlank()) {
                    Text(
                        text = customerName,
                        style = AppTheme.typography.heading5Semibold,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                Text(
                    text = "Kode transaksi #${System.currentTimeMillis().toString().takeLast(6)}", // Contoh kode transaksi
                    style = AppTheme.typography.paragraph1Medium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Color.LightGray)
                // Detail Transaksi
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy | HH.mm")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tanggal/Waktu", style = AppTheme.typography.paragraph2)
                    Text(currentDateTime.format(formatter), style = AppTheme.typography.paragraph2Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Items", style = AppTheme.typography.paragraph2, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f), // Allow name to take up available space
                            horizontalAlignment = Alignment.Start
                        ) {
                            val displayText = "${item.name}"
                            if (displayText.length > 14) {
                                Text(
                                    text = "${item.name}",
                                    style = AppTheme.typography.paragraph2Semibold.merge(
                                        TextStyle(lineBreak = LineBreak.Paragraph)
                                    )
                                )
                            } else {
                                Text(
                                    text = displayText,
                                    style = AppTheme.typography.paragraph2Semibold
                                )
                            }
                        }
                        Text("x ${item.quantity} ", style = AppTheme.typography.paragraph2)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formatRupiah(item.price.toString()), style = AppTheme.typography.paragraph2Medium) // Tampilkan harga satuan
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pembayaran", style = AppTheme.typography.paragraph2, color = Color.Gray)
                    Text(
                        text = paymentMethod ?: "Tidak Diketahui",
                        style = AppTheme.typography.paragraph2Semibold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(thickness = 1.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Total transaksi",
                        style = AppTheme.typography.heading5Medium
                    )
                    Text(
                        text = formatRupiah(totalBiaya.toString()),
                        style = AppTheme.typography.heading5Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionReceiptPreview() {
    AppTheme {
        val dummyCartItems = listOf(
            CartItem(menuId = 1, name = "Nasi Rawon", quantity = 1, price = "10000.0", photo = ""),
            CartItem(menuId = 2, name = "Soto Ayam", quantity = 1, price = "10000.0", photo = ""),
            CartItem(menuId = 3, name = "Nasi Campurrtttttt", quantity = 1, price = "10000.0", photo = "")
        )
        TransactionReceipt(
            cartItems = dummyCartItems,
            totalBiaya = 30000.0,
            paymentMethod = "Tunai",
            customerName = "Pengguna Preview"
        )
    }
}