package dev.codcow.kasirku.features.pdf

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.transaksi.Data
import dev.codcow.kasirku.ui.theme.AppTheme

@Composable
fun ReceiptContent(transaction: Data, warung: dev.codcow.kasirku.core.data.model.warung.Data) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .width(200.dp)
    ) {
        Text(text = warung.name, style = AppTheme.typography.heading5Semibold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "No: ${transaction.id}")
        Text(text = "Tanggal: ${transaction.created_at}")
        Text(text = "Payment: ${transaction.payment_method}")
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        transaction.transaction_details.forEach { item ->
            Text("${item.menu_name} x${item.quantity} = Rp${item.subtotal}")
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(text = "TOTAL: Rp${transaction.total_amount}", style = AppTheme.typography.heading5Semibold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Terima Kasih", textAlign = TextAlign.Center)
    }
}
