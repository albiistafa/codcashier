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
    onDeleteClick: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Lihat detail transaksi", role = Role.Button) { onItemClick(transaksi.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column (
                modifier = Modifier.weight(1f)
            ){
                Text(
                    text = transaksi.nama_transaksi ,
                    style = AppTheme.typography.paragraph1Semibold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatTanggalSaja(transaksi.created_at),
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatRupiah(transaksi.total_amount),
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tombol Delete
            IconButton(onClick = { onDeleteClick(transaksi.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TransaksiItemPengeluaran(
    pengeluaran: TransactionPengeluaran,
    onDeleteClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pengeluaran.nama_transaksi,
                    style = AppTheme.typography.paragraph1Semibold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatRupiah(pengeluaran.total_amount),
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = { onDeleteClick(pengeluaran.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
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

