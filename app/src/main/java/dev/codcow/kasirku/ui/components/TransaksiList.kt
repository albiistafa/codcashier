package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.core.data.model.pegawai.Data
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.core.data.model.pengeluaran.TransactionPengeluaran
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransaksiItemDelete(
    transaksi: Transaction,
    index: Int,
    count: Int,
    onDeleteClick: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) {
    val pending = transaksi.status.equals("pending", ignoreCase = true)
    val method = transaksi.payment_method.orEmpty().lowercase()
    AdminRow(
        index = index, count = count,
        title = transaksi.nama_transaksi.ifBlank { "Tanpa nama" },
        subtitle = listOf(formatTanggalSaja(transaksi.created_at), method, if (pending) "belum dibayar" else "lunas")
            .filter { it.isNotBlank() }.joinToString(", "),
        // Only unpaid orders open (to be paid), as before
        onClick = if (pending) ({ onItemClick(transaksi.id) }) else null,
        leading = { RowIcon(when (method) { "tunai", "cash" -> AppIcons.cash; "deposit" -> AppIcons.card; else -> AppIcons.qr }) },
        trailing = {
            Text(formatRupiah(transaksi.total_amount).replace("Rp ", "Rp"), style = AppTheme.typography.paragraph1Bold)
            DeleteButton(transaksi.nama_transaksi) { onDeleteClick(transaksi.id) }
        }
    )
}

@Composable
fun TransaksiItemPengeluaran(
    pengeluaran: TransactionPengeluaran,
    index: Int,
    count: Int,
    onDeleteClick: (Int) -> Unit
) {
    AdminRow(
        index = index, count = count,
        title = pengeluaran.nama_transaksi.ifBlank { "Tanpa nama" },
        subtitle = formatTanggalSaja(pengeluaran.created_at),
        leading = { RowIcon(AppIcons.pengeluaran) },
        trailing = {
            Text("−" + formatRupiah(pengeluaran.total_amount).replace("Rp ", "Rp"), style = AppTheme.typography.paragraph1Bold)
            DeleteButton(pengeluaran.nama_transaksi) { onDeleteClick(pengeluaran.id) }
        }
    )
}

fun formatTanggalSaja(timestamp: String): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault())
        offsetDateTime.format(formatter)
    } catch (e: Exception) {
        "Format Tidak Valid"
    }
}

