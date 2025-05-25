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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val selectedMenu by menuViewModel.selectedMenu.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var showDepositDropdown by remember { mutableStateOf(false) }
    var selectedDepositOption by remember { mutableStateOf<String?>(null) }
    var selectedDepositId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    val depositList by depositViewModel.depositItem.collectAsState()
    val selectedDeposit = depositList.find { it.id == selectedDepositId }

    // Fetch transaction details when the screen is composed
    LaunchedEffect(transactionId) {
        transaksiViewModel.getTransactionById(transactionId)
    }

    // Update payment method when transaction is loaded
    LaunchedEffect(transaction) {
        transaction?.let {
            selectedPaymentMethod = it.payment_method
        }
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (transaction != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.onSurface)
                ) {
                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedPaymentMethod?.let { paymentMethod ->
                                    scope.launch {
                                        transaksiViewModel.updateStatus(

                                            status = "lunas",
                                            paymentMethod = paymentMethod,
                                            is_delivered = "yes",
                                            id = transactionId
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6D8E22)
                            ),
                            enabled = selectedPaymentMethod != null && selectedPaymentMethod != transaction?.payment_method
                        ) {
                            Text("Update Pembayaran")
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
                .background(AppTheme.colors.onSurface)
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
                        CircularProgressIndicator(color = Color(0xFF90C14F))
                    }
                } else if (error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${error ?: "Unknown error"}",
                            color = Color.Red,
                            style = AppTheme.typography.paragraph1
                        )
                    }
                } else {
                    transaction?.let { currentTransaction ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item { Divider() }
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
                                        focusedBorderColor = Color(0xFF90C14F),
                                        focusedLabelColor = Color(0xFF90C14F)
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

                            // Payment method section
                            item {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                    color = Color.LightGray
                                )

                                Text(
                                    "Pembayaran",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = AppTheme.typography.labelMedium
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PaymentMethodButton(
                                        text = "Tunai",
                                        selected = selectedPaymentMethod == "tunai",
                                        onClick = {
                                            selectedPaymentMethod = "tunai"
                                            selectedDepositOption = null
                                            selectedDepositId = null
                                        }
                                    )
                                    PaymentMethodButton(
                                        text = "Digital",
                                        selected = selectedPaymentMethod == "digital",
                                        onClick = {
                                            selectedPaymentMethod = "digital"
                                            selectedDepositOption = null
                                            selectedDepositId = null
                                        }
                                    )
                                    PaymentMethodButton(
                                        text = "Deposit",
                                        selected = selectedPaymentMethod == "deposit",
                                        onClick = {
                                            selectedPaymentMethod = "deposit"
                                            selectedDepositId = selectedDeposit?.customer_id
                                        }
                                    )
                                }
                            }

                            // Deposit dropdown
                            item {
                                if (selectedPaymentMethod == "deposit") {
                                    ExposedDropdownMenuBox(
                                        expanded = showDepositDropdown,
                                        onExpandedChange = { showDepositDropdown = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = selectedDepositOption ?: "",
                                            placeholder = { Text("Pilih Customer Deposit", style = AppTheme.typography.paragraph2) },
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDepositDropdown)
                                            },
                                            textStyle = AppTheme.typography.paragraph2,
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF90C14F),
                                                unfocusedBorderColor = Color(0xFF90C14F),
                                                focusedLabelColor = Color(0xFF90C14F),
                                                unfocusedLabelColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = showDepositDropdown,
                                            onDismissRequest = { showDepositDropdown = false },
                                            modifier = Modifier
                                                .background(Color.White)
                                                .exposedDropdownSize(matchTextFieldWidth = true)
                                        ) {
                                            depositList.forEach { option ->
                                                DropdownMenuItem(
                                                    modifier = Modifier.padding(horizontal = 0.dp),
                                                    text = {
                                                        Text(
                                                            text = option.customer_name,
                                                            style = AppTheme.typography.paragraph2
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedDepositOption = option.customer_name
                                                        selectedDepositId = option.id
                                                        selectedCustomerId = option.customer_id
                                                        showDepositDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.RemoveCircleOutline,
                        contentDescription = "Kurangi",
                        tint = Color(0xFF90C14F)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    style = AppTheme.typography.paragraph2
                )

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.AddCircleOutline,
                        contentDescription = "Tambah",
                        tint = Color(0xFF90C14F)
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
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Color(0xFFEEF5D2) else Color.Transparent,
            contentColor = if (selected) Color(0xFF546D1E) else Color.Black
        ),
        border = if (selected) BorderStroke(1.dp, Color(0xFF90C14F)) else BorderStroke(1.dp, Color.Black)
    )
        {
        Text(text, style = AppTheme.typography.paragraph2)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CartItemSwipeToDeletes(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToEnd -> false
                DismissValue.DismissedToStart -> {
                    onRemove()
                    true
                }
                else -> false
            }
        }
    )

    // Reset state jika item berubah
    LaunchedEffect(item.menuId) {
        if (dismissState.currentValue != DismissValue.Default) {
            dismissState.reset()
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        dismissThresholds = { direction ->
            FractionalThreshold(0.5f)
        },
        background = {
            val color by animateColorAsState(
                targetValue = when (dismissState.dismissDirection) {
                    DismissDirection.EndToStart -> Color.Red
                    else -> Color.Transparent
                },
                label = "Dismiss Background Color"
            )
            val iconAlpha by animateFloatAsState(
                targetValue = if (dismissState.dismissDirection == DismissDirection.EndToStart) 1f else 0f,
                label = "Dismiss Icon Alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color.White,
                    modifier = Modifier.alpha(iconAlpha)
                )
            }
        },
        dismissContent = {
            val itemAlpha by animateFloatAsState(
                targetValue = if (dismissState.isDismissed(DismissDirection.EndToStart)) 0f else 1f,
                label = "Item Alpha"
            )
            val itemScale by animateFloatAsState(
                targetValue = if (dismissState.isDismissed(DismissDirection.EndToStart)) 0.8f else 1f,
                label = "Item Scale"
            )
            CartItemRow(
                item = item,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRemove = onRemove,
                modifier = Modifier.graphicsLayer {
                    alpha = itemAlpha
                    scaleX = itemScale
                    scaleY = itemScale
                }
            )
        }
    )
}

fun Modifier.visibles(): Modifier = this.then(Modifier.alpha(1f))
fun Modifier.gones(): Modifier = this.then(Modifier.alpha(0f))

