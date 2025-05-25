package dev.codcow.kasirku.features.transaksi.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.model.transaksi.TransactionDetail
import dev.codcow.kasirku.core.data.model.transaksi.TransactionDetailX
import dev.codcow.kasirku.features.deposit.ui.screens.DepositViewModel
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuManagerViewModel
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.Result

private const val SERVICE_FEE = 500.00

@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananScreenSelesai(
    navController: NavController,
    transactionId: Int,
    depositViewModel: DepositViewModel = hiltViewModel(),
    menuViewModel: MenuManagerViewModel = hiltViewModel(),
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val transaction by transaksiViewModel.currentTransaction.collectAsState()
    val selectedMenu by menuViewModel.selectedMenu.collectAsState()

    // Fetch transaction details when the screen is composed
    LaunchedEffect(transactionId) {
        transaksiViewModel.getTransactionById(transactionId)
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
                    IconButton(onClick = onNavigateBack) {
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
                    // Transaction Items and Details in LazyColumn
                    transaction?.let { currentTransaction ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item { Divider() }

                            item { Spacer(modifier = Modifier.height(16.dp)) }

                            // Transaction Items List
                            items(
                                items = currentTransaction.transaction_details ?: emptyList(),
                                key = { item -> item.menu_id ?: item.hashCode() } // Ensure stable key
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
                                    onValueChange = { /* Do nothing, it's read-only */ },
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
                                    // Total Item
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

                                    // Biaya Layanan - tampilkan hanya jika total >= 10000
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

                                    // Total Cost - menambahkan SERVICE_FEE jika total >= 10000
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

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Pembayaran: ",
                                        style = AppTheme.typography.labelMedium
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50), // membuat bentuk pill
                                        color = Color(0xFFEAF5C9), // warna hijau muda seperti di gambar
                                        border = BorderStroke(1.dp, Color(0xFF2E4700)), // warna hijau tua
                                    ) {
                                        Text(
                                            text = "${currentTransaction.payment_method ?: ""}",
                                            color = Color(0xFF2E4700),
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            style = AppTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }

                            // Add some extra space at the bottom
                            item { Spacer(modifier = Modifier.height(30.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    item: TransactionDetailX,
    menuViewModel: MenuManagerViewModel = hiltViewModel()
) {
    val menuPhoto = rememberSaveable(item.menu_id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(item.menu_id) {
        item.menu_id?.let { menuId ->
            scope.launch {
                try {
                    menuViewModel.getMenuById(menuId)
                    // Tunggu sebentar untuk memastikan data sudah diupdate
                    delay(100)
                    menuViewModel.selectedMenu.collect { menu ->
                        if (menu?.id == menuId) {
                            menuPhoto.value = menu.photo
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TransactionItemRow", "Error fetching menu: ${e.message}")
                }
            }
        }
    }

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
                model = menuPhoto.value,
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
                    text = item.menu_name,
                    style = AppTheme.typography.paragraph1Bold
                )
                Text(
                    text = formatRupiah(item.menu_price.toString()),
                    style = AppTheme.typography.paragraph2
                )
            }

            // Display quantity (read-only)
            Text(
                text = "x${item.quantity}",
                style = AppTheme.typography.paragraph2Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}