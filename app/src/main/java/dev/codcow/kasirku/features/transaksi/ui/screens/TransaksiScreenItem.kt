package dev.codcow.kasirku.features.transaksi.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TransaksiItemTransaksi(
    transaction: Transaction,
    onFinishTransaction: (Transaction) -> Unit = {},
    onAntarTransaction: (Transaction) -> Unit = {},
    onItemClick: (Transaction) -> Unit = {}
    ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatJamSaja(transaction.created_at),
                    style = AppTheme.typography.paragraph2,
                    color = Color.Gray
                )
                Text(
                    text = "#${transaction.id}", // Assuming your Transaction model has an 'id'
                    style = AppTheme.typography.paragraph2,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = transaction.nama_transaksi.takeIf { !it.isNullOrEmpty() } ?: "No Name",
                style = AppTheme.typography.paragraph1Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Pembayaran :",
                    style = AppTheme.typography.paragraph2,
                    color = Color.Gray
                )
                Text(
                    text = formatRupiah(transaction.total_amount),
                    style = AppTheme.typography.paragraph2,
                    color = AppTheme.colors.surface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row (verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()){
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusTag(text = transaction.status)
                    Spacer(modifier = Modifier.width(4.dp))
                    if (!transaction.payment_method.isNullOrEmpty()) {
                        StatusTag(text = transaction.payment_method)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (transaction.is_delivered.equals("no", ignoreCase = true)) {
                        Button(
                            onClick = { onAntarTransaction(transaction) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surface), // Use your primary color
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                "Selesai",
                                color = Color.White,
                                style = AppTheme.typography.paragraph2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (transaction.status.equals("pending", ignoreCase = true)) {
                        Button(
                            onClick = { onFinishTransaction(transaction) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surface), // Use your primary color
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                "Bayar",
                                color = Color.White,
                                style = AppTheme.typography.paragraph2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Detail",
                        tint = AppTheme.colors.surface,
                        modifier = Modifier.clickable { onItemClick(transaction) } // Make the arrow clickable too
                    )
                }
            }
        }
    }
}

@Composable
fun StatusTag(text: String) {
    val lowerText = text.lowercase()

    val backgroundColor = when (lowerText) {
        "lunas" -> Color(0xFF0386DE)
        "pending" -> Color(0xFFFF8040)
        "digital", "tunai", "deposit" -> Color.White
        else -> Color.Transparent
    }

    val isWhiteBackground = backgroundColor == Color.White
    val hasBackground = backgroundColor != Color.Transparent

    val textColor = when {
        isWhiteBackground -> Color(0xFF8BC34A)
        hasBackground -> Color.White
        else -> Color(0xFF8BC34A)
    }

    val borderWidth = if (isWhiteBackground || !hasBackground) 1.dp else 0.dp
    val borderColor = Color(0xFF8BC34A)
    val shape = RoundedCornerShape(10.dp)
    val horizontalPadding = if (hasBackground) 8.dp else 7.dp
    val verticalPadding = if (hasBackground) 4.dp else 3.dp

    Box(
        modifier = Modifier
            .border(width = borderWidth, color = borderColor, shape = shape)
            .background(color = backgroundColor, shape = shape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = "#${text.replaceFirstChar { it.uppercaseChar() }}",
            style = AppTheme.typography.paragraph2.copy(fontWeight = FontWeight.Light),
            color = textColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransaksiItemWithLongNamePreview() {
    AppTheme {
        val sampleTransaction = Transaction(
            id = 789,
            nama_transaksi = "Melani",
            total_amount = "25000",
            created_at = "2025-04-20T18:45:00.000Z",
            payment_method = "deposit",
            status = "Pending",
            transaction_details = emptyList(),
            updated_at = "",
            user_id = 1,
            is_delivered = ""
        )
        TransaksiItemTransaksi(transaction = sampleTransaction)
    }
}