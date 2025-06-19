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


@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananScreen(
    navController: NavController,
    depositViewModel: DepositViewModel = hiltViewModel(),
    menuViewModel: MenuManagerViewModel = hiltViewModel(),
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val isSuccess by transaksiViewModel.isSuccess.collectAsState()
    val cart by menuViewModel.cart.collectAsState()
    val scope = rememberCoroutineScope()
    val depositList by depositViewModel.depositItem.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var customerName by remember { mutableStateOf("") }

    var showDepositDropdown by remember { mutableStateOf(false) }
    var selectedDepositOption by remember { mutableStateOf<String?>(null) }

    var selectedDepositId by remember { mutableStateOf<Int?>(null) }
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }

    val selectedDeposit = depositList.find { it.id == selectedDepositId }

    var insufficientBalance by remember { mutableStateOf(false) }
    var showBalanceDialog by remember { mutableStateOf(false) }

    val totalItem by remember(cart) { derivedStateOf { cart.totalPrice } }
    val totalBiaya by remember(cart) { derivedStateOf { cart.totalPrice } }

    val currentBalance = selectedDeposit?.balance?.toDoubleOrNull() ?: 0.0
    val hasEnoughBalance = currentBalance >= totalBiaya

    val itemQuantities = remember { mutableStateMapOf<Int, Int>() }
    val currentTransactionId by transaksiViewModel.currentTransactionId.collectAsState()
    val currentStatusDetail by transaksiViewModel.currentStatusDetail.collectAsState()

    // Tambahkan flag untuk mencegah navigasi otomatis
    var shouldPreventAutoNavigation by remember { mutableStateOf(false) }

    var showCustomerNameSuggestions by remember { mutableStateOf(false) }
    val customerNameHistory by transaksiViewModel.customerNameHistory.collectAsState()

    LaunchedEffect(Unit) {
        depositViewModel.getAllDeposits()
    }

    LaunchedEffect(selectedDeposit) {
        selectedDeposit?.let {
            customerName = it.customer_name
            selectedCustomerId = it.customer_id
            insufficientBalance = !hasEnoughBalance
        } ?: run {
            if (selectedPaymentMethod != "deposit") {
                customerName = ""
                selectedCustomerId = null
                insufficientBalance = false
            }
        }
    }

    // Solusi sederhana: Gabungkan logika dalam satu LaunchedEffect

    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess, currentTransactionId, currentStatusDetail) {
        if (isSuccess && !hasNavigated && currentTransactionId != null) {
            Log.d("DetailPesananScreen", "Transaksi Berhasil")

            hasNavigated = true // Prevent multiple navigation

            // Clear cart
            menuViewModel.clearCartAndWait()

            // Add longer delay to ensure everything is processed
            delay(300)

            if (currentStatusDetail == "pending") {
                Log.d("DetailPesananScreen", "Navigating to Menu")
                navController.navigate(Screen.Menu.route) {
                    popUpTo(0) { inclusive = true } // Clear entire stack
                }
            } else {
                Log.d("DetailPesananScreen", "Navigating to DetailPembayaran")
                navController.navigate(Screen.DetailPembayaran.createRoute(currentTransactionId!!)) {
                    popUpTo(0) { inclusive = true } // Clear entire stack
                }
            }
        }
    }

// Simplify cart monitoring - only check when not in transaction process
    LaunchedEffect(cart.items.size) {
        if (!isLoading && !isSuccess && !hasNavigated) {
            delay(300) // Shorter delay
            val hasItems = cart.items.any { it.quantity > 0 }

            if (!hasItems) {
                Log.d("DetailPesananScreen", "No items, returning to menu")
                navController.navigate(Screen.Menu.route) {
                    popUpTo(Screen.Menu.route) { inclusive = true }
                }
            }
        }
    }

//    LaunchedEffect(isSuccess) {
//        if (isSuccess) {
//            Log.d("DetailPesananScreen", "Transaksi Berhasil, Clearing Cart")
//
//            // Clear cart dan tunggu sampai selesai
//            menuViewModel.clearCartAndWait()
//
//            // Tunggu sampai currentTransactionId tersedia
//            if (currentTransactionId != null) {
//                if (currentStatusDetail == "pending") {
//                    Log.d("DetailPesananScreen", "Transaksi Pending, Kembali ke Menu")
//                    navController.navigate(Screen.Menu.route) {
//                        popUpTo(Screen.Menu.route) { inclusive = true }
//                    }
//                } else {
//                    Log.d("DetailPesananScreen", "Navigating to DetailPembayaran with ID: $currentTransactionId")
//                    navController.navigate(Screen.DetailPembayaran.createRoute(currentTransactionId!!)) {
//                        popUpTo(Screen.Menu.route) { inclusive = true }
//                    }
//                }
//            }
//        }
//    }
//
//    LaunchedEffect(cart) {
//        Log.d("DetailPesananScreen", "Cart Updated: ${cart.items.size} items")
//        // Tunggu sebentar untuk memastikan cart sudah terisi
//        kotlinx.coroutines.delay(500)
//        val hasItems = cart.items.any { it.quantity > 0 }
//        Log.d("DetailPesananScreen", "Has items after delay: $hasItems, items: ${cart.items.map { it.quantity }}")
//
//        // Hanya navigasi ke menu jika cart kosong dan bukan karena transaksi berhasil
//        if (!hasItems && !isLoading && !shouldPreventAutoNavigation) {
//            Log.d("DetailPesananScreen", "No items in cart, navigating back to menu")
//            navController.navigate(Screen.Menu.route) {
//                popUpTo(Screen.Menu.route) { inclusive = true }
//            }
//        }
//    }

    if (showBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            title = { Text(
                "Saldo Tidak Cukup",
                style = AppTheme.typography.heading4Bold,
                color = AppTheme.colors.surface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            ) },
            text = {
                Text("Saldo deposit pelanggan tidak cukup untuk transaksi ini. " +
                        "Saldo saat ini ${formatRupiah(currentBalance.toString())}, " +
                        "Total biaya ${formatRupiah(totalBiaya.toString())}"
                    ,textAlign = TextAlign.Justify)
            },
            confirmButton = {
                TextButton(onClick = { showBalanceDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppTheme.colors.surface // Warna tulisan tombol OK
                    )) {
                    Text("OK")
                }
            },
            containerColor = AppTheme.colors.onSurface, // Warna background AlertDialog
            textContentColor = Color.Black
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Pesanan",
                        style = AppTheme.typography.labelBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { /* Shopping cart action */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = AppTheme.colors.surface
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Move action buttons to bottom bar to ensure they're always accessible
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
                            if (customerName.isNotBlank()) {
                                transaksiViewModel.addToCustomerNameHistory(customerName)
                            }
                            if (selectedPaymentMethod != null) {
                                // Check if using deposit but has insufficient balance
                                if (selectedPaymentMethod == "deposit" && insufficientBalance) {
                                    showBalanceDialog = true
                                    return@Button
                                }
                                scope.launch {
                                    menuViewModel.setPaymentMethod(selectedPaymentMethod!!)
                                    val transactionData = if (selectedPaymentMethod == "deposit") {
                                        Log.d("TransactionScreen", "Using deposit with Customer ID: $selectedCustomerId")
                                        menuViewModel.prepareTransaction(customerName, customerId = selectedCustomerId)
                                    } else {
                                        menuViewModel.prepareTransaction(customerName, customerId = null)
                                    }
                                    transaksiViewModel.createTransaction(transactionData)
                                    Log.d("TRANSACTION_DETAILS", "Details: ${transactionData.nama_transaksi}")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6D8E22)
                        ),
                        enabled = selectedPaymentMethod != null &&
                                customerName.isNotBlank() &&
                                cart.items.isNotEmpty()
                    ) {
                        Text("Bayar")
                    }

                    OutlinedButton(
                        onClick = {
                            if (customerName.isNotBlank()) {
                                transaksiViewModel.addToCustomerNameHistory(customerName)
                            }

                            scope.launch {
                                if (customerName.isNotBlank()) {
                                    transaksiViewModel.addToCustomerNameHistory(customerName)
                                }

                                menuViewModel.setPaymentMethod(selectedPaymentMethod)

                                // Handle payment method logic
                                val transactionData = when {
                                    selectedPaymentMethod == "deposit" -> {
                                        // Check if using deposit but has insufficient balance
                                        if (insufficientBalance) {
                                            showBalanceDialog = true
                                            return@launch
                                        }
                                        Log.d("TransactionScreen", "Using deposit with Customer ID: $selectedCustomerId")
                                        menuViewModel.prepareTransactionPending(customerName, customerId = selectedCustomerId)
                                    }
                                    selectedPaymentMethod != null -> {
                                        // Payment method selected but not deposit
                                        menuViewModel.prepareTransactionPending(customerName, customerId = null)
                                    }
                                    else -> {
                                        // No payment method selected - send null
                                        menuViewModel.prepareTransactionPending(customerName, customerId = null)
                                    }
                                }

                                transaksiViewModel.createTransaction(transactionData)
                                Log.d("TRANSACTION_DETAILS", "Details: ${transactionData.nama_transaksi}")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF90C14F)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF90C14F))
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    ) { paddingValues ->
        // Main content in a scrollable column
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .background(AppTheme.colors.onSurface)
        ) {
            // Make entire content scrollable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Divider() }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Cart Items List
                    items(
                        items = cart.items,
                        key = { it.menuId }
                    ) { item ->
                        Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)){
                            CartItemSwipeToDelete(
                                item = item,
                                onIncrement = {
                                    scope.launch {
                                        menuViewModel.updateCartItemQuantity(item.menuId, item.quantity + 1)
                                    }
                                },
                                onDecrement = {
                                    scope.launch {
                                        if (item.quantity > 1) {
                                            menuViewModel.updateCartItemQuantity(item.menuId, item.quantity - 1)
                                        } else {
                                            menuViewModel.removeFromCart(item.menuId)
                                        }
                                    }
                                },
                                onRemove = {
                                    scope.launch {
                                        menuViewModel.removeFromCart(item.menuId)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Add order button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .drawBehind {
                                    val strokeWidth = 3f
                                    val dashWidth = 10f
                                    val dashGap = 10f
                                    val paint = Paint().apply {
                                        color = Color(0xFF6D8E22)
                                        style = PaintingStyle.Stroke
                                        this.strokeWidth = strokeWidth
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                                    }
                                    drawRoundRect(
                                        color = Color(0xFF6D8E22),
                                        topLeft = Offset.Zero,
                                        size = size,
                                        cornerRadius = CornerRadius(10.dp.toPx()),
                                        style = Stroke(
                                            width = 3f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f))
                                        )
                                    )
                                }
                        ) {
                            OutlinedButton(
                                onClick = { navController.navigate("menu") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF6D8E22),
                                    containerColor = Color(0x33C4DE78)
                                ),
                                border = BorderStroke(0.dp, Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text("Tambah Pesanan", style = AppTheme.typography.paragraph1)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Outlined.AddCircleOutline, contentDescription = "Tambah")
                                }
                            }
                        }
                    }

                    // Customer Name TextField
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Track if the text field is focused
                            var isFocused by remember { mutableStateOf(false) }

                            // Standard OutlinedTextField
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = {
                                    customerName = it
                                    showCustomerNameSuggestions = it.isNotBlank() || isFocused
                                },
                                textStyle = AppTheme.typography.paragraph2,
                                placeholder = { Text("Nama Pelanggan/Keterangan", style = AppTheme.typography.paragraph2) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .onFocusChanged {
                                        isFocused = it.isFocused
                                        if (it.isFocused) {
                                            showCustomerNameSuggestions = true
                                        }
                                    },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF90C14F),
                                    focusedLabelColor = Color(0xFF90C14F)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            // Gunakan dropdown menu standar, bukan exposed dropdown
                            DropdownMenu(
                                expanded = showCustomerNameSuggestions && isFocused,
                                onDismissRequest = { showCustomerNameSuggestions = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)  // Sedikit lebih kecil dari parent
                                    .background(Color.White),
                                properties = PopupProperties(focusable = false)  // Kunci! Jangan ambil fokus dari TextField
                            ) {
                                // Filter histori berdasarkan input pengguna
                                val filteredNames = if (customerName.isBlank()) {
                                    customerNameHistory
                                } else {
                                    customerNameHistory.filter {
                                        it.contains(customerName, ignoreCase = true)
                                    }
                                }

                                if (filteredNames.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "No history found",
                                                style = AppTheme.typography.paragraph2,
                                                color = Color.Gray
                                            )
                                        },
                                        onClick = { /* Tidak melakukan apa-apa */ },
                                        enabled = false
                                    )
                                } else {
                                    filteredNames.forEach { name ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    name,
                                                    style = AppTheme.typography.paragraph2
                                                )
                                            },
                                            onClick = {
                                                customerName = name
                                                showCustomerNameSuggestions = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Total calculations
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Total Item
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Item", style = AppTheme.typography.paragraph1)
                                Text(formatRupiah(cart.totalPrice.toString()), style = AppTheme.typography.paragraph1)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Total Cost
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total Biaya",
                                    style = AppTheme.typography.paragraph1Bold
                                )
                                Text(
                                    formatRupiah(totalBiaya.toString()),
                                    style = AppTheme.typography.paragraph1Bold
                                )
                            }
                        }
                    }

                    // Deposit info
                    item {
                        if (selectedPaymentMethod == "deposit" && selectedDeposit != null) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Saldo Deposit",
                                    style = AppTheme.typography.paragraph2
                                )
                                Text(
                                    formatRupiah(selectedDeposit.balance),
                                    style = AppTheme.typography.paragraph2,
                                    color = if (insufficientBalance) Color.Red else Color(0xFF6D8E22)
                                )
                            }

                            if (insufficientBalance) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Saldo tidak cukup!",
                                    style = AppTheme.typography.paragraph2,
                                    color = Color.Red,
                                    modifier = Modifier.padding(start = 16.dp)
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

                                                val balance = option.balance.toDoubleOrNull() ?: 0.0
                                                insufficientBalance = balance < totalBiaya
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

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF90C14F))
            }
        }
    }
}


@Composable
fun CartItemRow(
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
 fun PaymentMethodButton(
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
fun CartItemSwipeToDelete(
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

fun Modifier.visible(): Modifier = this.then(Modifier.alpha(1f))
fun Modifier.gone(): Modifier = this.then(Modifier.alpha(0f))

