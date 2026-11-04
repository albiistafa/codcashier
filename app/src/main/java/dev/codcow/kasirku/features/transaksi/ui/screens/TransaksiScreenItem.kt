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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.components.DuoIcon
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TransaksiItemTransaksi(
    transaction: Transaction,
    divider: Boolean = false,
    onFinishTransaction: (Transaction) -> Unit = {},
    onAntarTransaction: (Transaction) -> Unit = {},
    onItemClick: (Transaction) -> Unit = {}
) {
    val pending = transaction.status.equals("pending", ignoreCase = true)
    val undelivered = transaction.is_delivered.equals("no", ignoreCase = true)
    val method = transaction.payment_method.orEmpty().lowercase()
    val icon = when (method) {
        "tunai", "cash" -> AppIcons.cash
        "deposit" -> AppIcons.card
        else -> AppIcons.qr
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Lihat detail") { onItemClick(transaction) }
    ) {
        Row(Modifier.heightIn(min = 68.dp).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { DuoIcon(icon, 22.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    transaction.nama_transaksi.takeIf { !it.isNullOrEmpty() } ?: "Tanpa nama",
                    style = AppTheme.typography.paragraph1Semibold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${formatJamSaja(transaction.created_at)}, ${if (pending) "belum dibayar" else "lunas"}",
                    style = AppTheme.typography.paragraph2,
                    color = if (pending) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatRupiah(transaction.total_amount).replace("Rp ", "Rp"), style = AppTheme.typography.paragraph1Bold)
        }
        if (pending || undelivered) {
            Row(
                Modifier.fillMaxWidth().padding(start = 52.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (undelivered) {
                    Button(
                        onClick = { onAntarTransaction(transaction) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.heightIn(min = 40.dp)
                    ) { Text("Sudah diantar", style = AppTheme.typography.paragraph2Semibold) }
                }
                if (pending) {
                    Button(
                        onClick = { onFinishTransaction(transaction) },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.heightIn(min = 40.dp)
                    ) { Text("Bayar", style = AppTheme.typography.paragraph2Semibold) }
                }
            }
        }
        if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
            style = AppTheme.typography.paragraph2,
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
