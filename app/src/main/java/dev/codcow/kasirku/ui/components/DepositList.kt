package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

/**
 * Pelanggan deposit: saldo on the right with an "Isi saldo" money button; tap the row to edit.
 * Deleting moved to the edit screen so the row holds one action besides opening it.
 */
@Composable
fun DepositItem(
    deposit: DataDeposit,
    index: Int,
    count: Int,
    onTopUpClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val saldo = deposit.balance.toDoubleOrNull() ?: 0.0
    AdminRow(
        index = index, count = count,
        title = deposit.customer_name,
        subtitle = deposit.phone_number,
        onClick = onItemClick,
        leading = { InitialsAvatar(deposit.customer_name) },
        trailing = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    formatRupiah(saldo.toString()).replace("Rp ", "Rp"),
                    style = AppTheme.typography.paragraph1Bold,
                    color = if (saldo <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onTopUpClick,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.money, contentColor = AppTheme.colors.onMoney),
                    modifier = Modifier.heightIn(min = 36.dp)
                ) { Text("Isi saldo", style = AppTheme.typography.paragraph2Semibold) }
            }
        }
    )
}
