package dev.codcow.kasirku.features.transaksi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.components.DuoIcon
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
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

private fun rp(amount: Long) = formatRupiah(amount.toDouble().toString()).replace("Rp ", "Rp")

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
                    "Total tagihan, ${cart.items.sumOf { it.quantity }} item",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(rp(total), style = AppTheme.typography.heading3Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            // Three icon tiles, like the payment pickers in Grab/Gojek
            val methods = listOf(Triple("tunai", "Tunai", AppIcons.cash), Triple("digital", "Digital", AppIcons.qr), Triple("deposit", "Deposit", AppIcons.card))
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { (value, label, icon) ->
                    val selected = method == value
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .selectable(selected = selected, role = Role.RadioButton) { method = value }
                    ) {
                        Column(
                            Modifier.heightIn(min = 76.dp).padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
                        ) {
                            DuoIcon(icon, 28.dp)
                            Text(label, style = if (selected) AppTheme.typography.paragraph2Semibold.copy(fontWeight = FontWeight.Bold) else AppTheme.typography.paragraph2Semibold)
                        }
                    }
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
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.money, contentColor = AppTheme.colors.onMoney),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) {
                    if (busy && submitted == false) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = AppTheme.colors.onMoney,
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
                            style = AppTheme.typography.paragraph1Bold
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
                        style = AppTheme.typography.paragraph1Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            quickCashOptions(total).forEachIndexed { i, amount ->
                val selected = customAmount.isEmpty() && received == amount
                Surface(
                    onClick = { onPick(amount) },
                    shape = CircleShape,
                    color = if (selected) AppTheme.colors.deep else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.semantics { this.selected = selected }
                ) {
                    Text(
                        if (i == 0) "Uang pas" else rp(amount).removePrefix("Rp"),
                        style = AppTheme.typography.paragraph2Semibold,
                        modifier = Modifier.heightIn(min = 44.dp).wrapContentHeight().padding(horizontal = 16.dp)
                    )
                }
            }
        }
        OutlinedTextField(
            value = customAmount,
            onValueChange = onCustomChange,
            label = { Text("Nominal lain") },
            prefix = { Text("Rp ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        val short = change != null && change < 0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (short) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fg = if (short) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            Text(if (short) "Kurang" else "Kembalian", style = AppTheme.typography.paragraph1Medium, color = fg, modifier = Modifier.weight(1f))
            Text(change?.let { rp(abs(it)) } ?: "—", style = AppTheme.typography.heading4Bold, color = fg)
        }
    }
}
