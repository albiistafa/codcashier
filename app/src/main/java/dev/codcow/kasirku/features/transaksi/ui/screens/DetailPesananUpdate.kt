package dev.codcow.kasirku.features.transaksi.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.features.deposit.ui.screens.DepositViewModel
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuManagerViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SERVICE_FEE = 500.00

@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananScreenUpdate(
    navController: NavController,
    transactionId: Int,
    depositViewModel: DepositViewModel = hiltViewModel(),
    menuViewModel: MenuManagerViewModel = hiltViewModel(),
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val isSuccess by transaksiViewModel.isSuccess.collectAsState()
    val transaction by transaksiViewModel.currentTransaction.collectAsState()

    var selectedPaymentMethod by rememberSaveable(transactionId) { mutableStateOf<String?>(null) }
    var selectedCustomerId by rememberSaveable(transactionId) { mutableStateOf<Int?>(null) }
    var selectionInitialized by rememberSaveable(transactionId) { mutableStateOf(false) }
    var showDepositDropdown by remember { mutableStateOf(false) }
    val depositList by depositViewModel.depositItem.collectAsState()
    val depositLoading by depositViewModel.isLoading.collectAsState()
    val depositError by depositViewModel.error.collectAsState()
    val selectedDeposit = depositList.find { it.customer_id == selectedCustomerId }
    val amount = transaction?.total_amount?.toBigDecimalOrNull()
    val balance = selectedDeposit?.balance?.toBigDecimalOrNull()
    val depositReady = selectedDeposit != null && amount != null && balance != null && balance >= amount

    LaunchedEffect(transactionId) {
        transaksiViewModel.resetState()
        transaksiViewModel.getTransactionById(transactionId)
    }
    LaunchedEffect(transaction?.id) {
        transaction?.let {
            if (!selectionInitialized) {
                selectedPaymentMethod = it.payment_method?.takeIf { method -> method in listOf("tunai", "digital", "deposit") }
                selectedCustomerId = it.customer_id
                selectionInitialized = true
            }
        }
    }
    LaunchedEffect(selectedPaymentMethod) {
        if (selectedPaymentMethod == "deposit") depositViewModel.getAllDeposits()
    }

    // Handle success state
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Update Pesanan",
                        style = AppTheme.typography.labelBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (transaction != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedPaymentMethod?.let { method ->
                                    transaksiViewModel.payTransaction(method, selectedCustomerId)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = !isLoading && transaction?.status != "lunas" && selectedPaymentMethod != null &&
                                (selectedPaymentMethod != "deposit" || (!depositLoading && depositError == null && depositReady))
                        ) {
                            Text(if (isLoading) "Menyimpan…" else if (transaction?.status == "lunas") "Sudah dibayar" else "Bayar")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    if (transaction == null) {
                        Column(Modifier.padding(16.dp)) {
                            Text(error ?: "Transaksi belum tersedia", color = MaterialTheme.colorScheme.onSurface)
                            Button(onClick = { transaksiViewModel.getTransactionById(transactionId) }) { Text("Coba lagi") }
                        }
                    }
                    transaction?.let { currentTransaction ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (error != null) {
                                item {
                                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                                }
                            }
                            item { HorizontalDivider() }
                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            // Transaction Items List
                            items(
                                items = currentTransaction.transaction_details ?: emptyList(),
                                key = { item -> item.menu_id ?: item.hashCode() }
                            ) { item ->
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                ) {
                                    TransactionItemRow(item = item)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Customer Name Text
                            item {
                                OutlinedTextField(
                                    value = currentTransaction.nama_transaksi ?: "",
                                    onValueChange = { },
                                    textStyle = AppTheme.typography.paragraph2,
                                    placeholder = { Text("Nama Pelanggan/Keterangan", style = AppTheme.typography.paragraph2) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(48.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    readOnly = true
                                )
                            }

                            // Total calculations
                            item {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Item", style = AppTheme.typography.paragraph1)
                                        Text(
                                            formatRupiah(currentTransaction.total_amount),
                                            style = AppTheme.typography.paragraph1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val totalAmount = currentTransaction.total_amount.toDoubleOrNull() ?: 0.0
                                    if (totalAmount >= 10000) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Biaya Layanan", style = AppTheme.typography.paragraph1)
                                            Text(formatRupiah(SERVICE_FEE.toString()), style = AppTheme.typography.paragraph1)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    val totalCost = totalAmount

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Total Biaya",
                                            style = AppTheme.typography.paragraph1Bold
                                        )
                                        Text(
                                            formatRupiah(totalCost.toString()),
                                            style = AppTheme.typography.paragraph1Bold
                                        )
                                    }
                                }
                            }

                            item {
                                Column(Modifier.fillMaxWidth().padding(16.dp).selectableGroup()) {
                                    Text("Metode pembayaran", style = AppTheme.typography.labelMedium)
                                    listOf("tunai" to "Tunai", "digital" to "Digital", "deposit" to "Deposit").forEach { (method, label) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                                .selectable(
                                                    selected = selectedPaymentMethod == method,
                                                    enabled = currentTransaction.status != "lunas",
                                                    role = Role.RadioButton,
                                                    onClick = { selectedPaymentMethod = method }
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = selectedPaymentMethod == method, onClick = null)
                                            Text(label, modifier = Modifier.padding(start = 12.dp))
                                        }
                                    }
                                    if (selectedPaymentMethod == "deposit") {
                                        ExposedDropdownMenuBox(
                                            expanded = showDepositDropdown,
                                            onExpandedChange = { if (!depositLoading) showDepositDropdown = it }
                                        ) {
                                            OutlinedTextField(
                                                value = selectedDeposit?.customer_name.orEmpty(),
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Pelanggan deposit") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showDepositDropdown) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(expanded = showDepositDropdown, onDismissRequest = { showDepositDropdown = false }) {
                                                depositList.forEach { deposit ->
                                                    DropdownMenuItem(
                                                        text = { Text(deposit.customer_name) },
                                                        onClick = {
                                                            selectedCustomerId = deposit.customer_id
                                                            showDepositDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        when {
                                            depositLoading -> Text("Memuat pelanggan deposit…")
                                            depositError != null -> {
                                                Text("Pelanggan deposit belum dapat dimuat.", color = MaterialTheme.colorScheme.error)
                                                TextButton(onClick = { depositViewModel.getAllDeposits() }) { Text("Coba lagi") }
                                            }
                                            depositList.isEmpty() -> Text("Belum ada pelanggan deposit. Pilih metode pembayaran lain.")
                                            selectedDeposit != null -> {
                                                Text("Saldo: ${formatRupiah(selectedDeposit.balance)}")
                                                if (!depositReady) Text("Saldo tidak mencukupi atau nilai transaksi tidak valid.", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRows(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu Image
            AsyncImage(
                model = item.photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
            )

            // Menu Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.paragraph1Bold
                )
                Text(
                    text = formatRupiah(item.price.toDouble().toString()),
                    style = AppTheme.typography.paragraph2
                )
            }

            // Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.RemoveCircleOutline,
                        contentDescription = "Kurangi",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    style = AppTheme.typography.paragraph2
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.AddCircleOutline,
                        contentDescription = "Tambah",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
 fun PaymentMethodButtons(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    )
        {
        Text(text, style = AppTheme.typography.paragraph2)
    }
}

