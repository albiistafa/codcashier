package dev.codcow.kasirku.features.transaksi.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "#${transaction.id}", // Assuming your Transaction model has an 'id'
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = transaction.nama_transaksi.takeIf { !it.isNullOrEmpty() } ?: "Tanpa nama",
                style = AppTheme.typography.paragraph1Bold,
                color = MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatRupiah(transaction.total_amount),
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusTag(text = transaction.status)
                    Spacer(modifier = Modifier.width(4.dp))
                    if (!transaction.payment_method.isNullOrEmpty()) {
                        StatusTag(text = transaction.payment_method)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (transaction.is_delivered.equals("no", ignoreCase = true)) {
                        Button(
                            onClick = { onAntarTransaction(transaction) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                "Selesai",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = AppTheme.typography.paragraph2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (transaction.status.equals("pending", ignoreCase = true)) {
                        Button(
                            onClick = { onFinishTransaction(transaction) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                "Bayar",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = AppTheme.typography.paragraph2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = { onItemClick(transaction) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Lihat detail transaksi ${transaction.nama_transaksi.orEmpty()}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTag(text: String) {
    val lowerText = text.lowercase()

    val scheme = MaterialTheme.colorScheme
    val backgroundColor = when (lowerText) {
        "lunas" -> scheme.primaryContainer
        "pending" -> scheme.secondaryContainer
        else -> scheme.surfaceVariant
    }
    val textColor = when (lowerText) {
        "lunas" -> scheme.onPrimaryContainer
        "pending" -> scheme.onSecondaryContainer
        else -> scheme.onSurfaceVariant
    }
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = shape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
