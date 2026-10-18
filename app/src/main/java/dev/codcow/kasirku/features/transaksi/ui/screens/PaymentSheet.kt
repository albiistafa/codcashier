package dev.codcow.kasirku.features.transaksi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.codcow.kasirku.features.deposit.ui.screens.DepositViewModel
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuManagerViewModel
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private fun rp(amount: Long) = formatRupiah(amount.toDouble().toString())

/** Exact amount plus the next notes a customer is likely to hand over. */
internal fun quickCashOptions(total: Long): List<Long> =
    listOf(total) + listOf(2_000L, 5_000L, 10_000L, 20_000L, 50_000L, 100_000L).filter { it > total }.take(3)

/**
 * One sheet for every way to finish an order: pay now (Tunai with change, Digital, Deposit)
 * or "Simpan dulu" as an unpaid order. Calls [onDone] once the server has the transaction;
 * [onDone]'s `received` is the cash handed over (Tunai only), for the change on the next screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSheet(
    menuViewModel: MenuManagerViewModel,
    onDismiss: () -> Unit,
    onDone: (transactionId: Int, pending: Boolean, received: Long?) -> Unit,
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    depositViewModel: DepositViewModel = hiltViewModel(),
) {
    val cart by menuViewModel.cart.collectAsState()
    val deposits by depositViewModel.depositItem.collectAsState()
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val isSuccess by transaksiViewModel.isSuccess.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val transactionId by transaksiViewModel.currentTransactionId.collectAsState()
    val status by transaksiViewModel.currentStatusDetail.collectAsState()
    val scope = rememberCoroutineScope()

    val total = cart.totalPrice.toLong()
    var method by remember { mutableStateOf("tunai") }
    var received by remember { mutableStateOf<Long?>(null) }
    var customAmount by remember { mutableStateOf("") }
    var depositId by remember { mutableStateOf<Int?>(null) }
    var name by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf<Boolean?>(null) } // null = not sent, else pending?
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { depositViewModel.getAllDeposits() }

    val deposit = deposits.find { it.id == depositId }
    val depositBalance = deposit?.balance?.toDoubleOrNull()?.toLong() ?: 0L
    val change = received?.let { it - total }

    LaunchedEffect(isSuccess, transactionId) {
        val pending = submitted ?: return@LaunchedEffect
        val id = transactionId
        if (isSuccess && id != null && !finished) {
            finished = true
            menuViewModel.clearCartAndWait()
            onDone(id, status == "pending" || pending, if (!pending && method == "tunai") received else null)
        }
    }

    val busy = isLoading && submitted != null
    val canPay = !busy && total > 0 && when (method) {
        "tunai" -> change != null && change >= 0
        "deposit" -> deposit != null && depositBalance >= total
        else -> true
    }
    val canSave = !busy && total > 0 && name.isNotBlank() && (method != "deposit" || deposit != null)

    fun submit(pending: Boolean) {
        val label = name.trim().ifBlank {
            // Server needs a name; an unnamed walk-in order still gets a findable one
            "Pesanan " + SimpleDateFormat("HH.mm", Locale("id", "ID")).format(Date())
        }
        transaksiViewModel.addToCustomerNameHistory(name.trim())
        submitted = pending
        scope.launch {
            menuViewModel.setPaymentMethod(method)
            val customerId = if (method == "deposit") deposit?.customer_id else null
            val data = if (pending) menuViewModel.prepareTransactionPending(label, customerId)
            else menuViewModel.prepareTransaction(label, customerId)
            transaksiViewModel.createTransaction(data)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    "Total tagihan · ${cart.items.sumOf { it.quantity }} item",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(rp(total), style = AppTheme.typography.heading3Bold, color = MaterialTheme.colorScheme.primary)
            }

            val methods = listOf("tunai" to "Tunai", "digital" to "Digital", "deposit" to "Deposit")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                methods.forEachIndexed { index, (value, label) ->
                    SegmentedButton(
                        selected = method == value,
                        onClick = { method = value },
                        shape = SegmentedButtonDefaults.itemShape(index, methods.size),
                        label = { Text(label, style = AppTheme.typography.paragraph2Semibold) }
                    )
                }
            }

            when (method) {
                "tunai" -> CashSection(
                    total = total,
                    received = received,
                    customAmount = customAmount,
                    onPick = { received = it; customAmount = "" },
                    onCustomChange = { raw ->
                        customAmount = raw.filter(Char::isDigit).take(9)
                        received = customAmount.toLongOrNull()
                    },
                    change = change
                )
                "digital" -> Text(
                    "Minta pelanggan membayar lewat QRIS atau transfer. Selesaikan setelah notifikasi pembayaran masuk.",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih pelanggan deposit", style = AppTheme.typography.paragraph2Semibold)
                    if (deposits.isEmpty()) {
                        Text(
                            "Belum ada pelanggan deposit.",
                            style = AppTheme.typography.paragraph2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    deposits.forEach { d ->
                        val balance = d.balance.toDoubleOrNull()?.toLong() ?: 0L
                        val enough = balance >= total
                        val selected = d.id == depositId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                if (selected) 2.dp else 1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = selected, role = Role.RadioButton) {
                                    depositId = d.id
                                    name = d.customer_name
                                }
                        ) {
                            Row(
                                modifier = Modifier.heightIn(min = 52.dp).padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(d.customer_name, style = AppTheme.typography.paragraph1Medium, modifier = Modifier.weight(1f))
                                Text(
                                    if (enough) "Saldo ${rp(balance)}" else "Saldo kurang · ${rp(balance)}",
                                    style = AppTheme.typography.paragraph2,
                                    color = if (enough) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (method == "deposit") "Nama pelanggan" else "Nama atau meja (opsional)") },
                placeholder = { Text("Contoh: Meja 4") },
                singleLine = true,
                textStyle = AppTheme.typography.paragraph1,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (submitted != null && error != null && !isLoading) {
                Text(
                    "Transaksi belum tersimpan: $error. Periksa koneksi lalu coba lagi.",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { submit(pending = false) },
                    enabled = canPay,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) {
                    if (busy && submitted == false) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        Text(
                            when (method) {
                                "tunai" -> when {
                                    change == null -> "Pilih uang diterima"
                                    change < 0 -> "Uang kurang"
                                    else -> "Terima ${rp(received!!)}"
                                }
                                "digital" -> "Sudah dibayar"
                                else -> when {
                                    deposit == null -> "Pilih pelanggan"
                                    depositBalance < total -> "Saldo tidak cukup"
                                    else -> "Potong saldo ${rp(total)}"
                                }
                            },
                            style = AppTheme.typography.paragraph1Semibold
                        )
                    }
                }
                TextButton(
                    onClick = { submit(pending = true) },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(
                        if (name.isBlank()) "Isi nama untuk simpan dulu" else "Simpan dulu, bayar nanti",
                        style = AppTheme.typography.paragraph1Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CashSection(
    total: Long,
    received: Long?,
    customAmount: String,
    onPick: (Long) -> Unit,
    onCustomChange: (String) -> Unit,
    change: Long?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Uang diterima", style = AppTheme.typography.paragraph2Semibold)
        quickCashOptions(total).withIndex().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (i, amount) ->
                    val selected = customAmount.isEmpty() && received == amount
                    OutlinedButton(
                        onClick = { onPick(amount) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            if (selected) 2.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Text(
                            if (i == 0) "Uang pas" else rp(amount),
                            style = AppTheme.typography.paragraph1Semibold
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        OutlinedTextField(
            value = customAmount,
            onValueChange = onCustomChange,
            label = { Text("Nominal lain") },
            prefix = { Text("Rp ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        val short = change != null && change < 0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (short) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fg = if (short) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
            Text(if (short) "Kurang" else "Kembalian", style = AppTheme.typography.paragraph1Medium, color = fg, modifier = Modifier.weight(1f))
            Text(change?.let { rp(abs(it)) } ?: "—", style = AppTheme.typography.heading4Bold, color = fg)
        }
    }
}
