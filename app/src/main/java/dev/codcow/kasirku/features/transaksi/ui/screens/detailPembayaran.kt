package dev.codcow.kasirku.features.transaksi.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import captureComposableToBitmap
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.core.data.model.warung.Data
import dev.codcow.kasirku.features.pdf.ReceiptContent
import dev.codcow.kasirku.features.pdf.createReceiptBitmap
import dev.codcow.kasirku.features.pdf.saveBitmapAsPdf
import dev.codcow.kasirku.features.warung.ui.screens.warungViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.TransactionReceipt
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPembayaran(
    navController: NavController,
    transactionId: Int,
    received: Long? = null,
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    warungViewModel: warungViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val warungName by warungViewModel.warung.collectAsState()
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val transaction by transaksiViewModel.currentTransaction.collectAsState()
    val context = LocalContext.current

    // This screen is the stack root after checkout; Back always lands on the menu
    fun backToMenu() {
        navController.navigate(Screen.Menu.route) { popUpTo(0) { inclusive = true } }
    }
    BackHandler { backToMenu() }

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("DetailPembayaran", "Screen initialized with transactionId: $transactionId")
    }

    LaunchedEffect(warungName) {
        Log.d("DetailPembayaran", "Warung name: ${warungName?.name ?: "null"}")
    }

    LaunchedEffect(transactionId) {
        Log.d("DetailPembayaran", "Fetching transaction with ID: $transactionId")
        transaksiViewModel.getTransactionById(transactionId)
    }

    LaunchedEffect(transaction) {
        Log.d("DetailPembayaran", "Transaction updated: ${transaction != null}")
        transaction?.let {
            Log.d("DetailPembayaran", "Transaction details: id=${it.id}, total=${it.total_amount}, method=${it.payment_method}")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun downloadReceipt() {
        val current = transaction ?: run {
            scope.launch { snackbarHostState.showSnackbar("Data transaksi belum tersedia") }
            return
        }
        val saved = runCatching {
            saveBitmapAsPdf(context, createReceiptBitmap(context, current, warungName))
        }.onFailure { Log.e("PDF_DEBUG", "Receipt PDF failed", it) }.getOrNull()
        scope.launch {
            if (saved != null) {
                snackbarHostState.showSnackbar("Struk berhasil diunduh")
            } else if (snackbarHostState.showSnackbar(
                    "Gagal mengunduh struk", actionLabel = "Coba lagi", duration = SnackbarDuration.Long
                ) == SnackbarResult.ActionPerformed
            ) {
                downloadReceipt()
            }
        }
    }

    LaunchedEffect(isLoading) {
        Log.d("DetailPembayaran", "Loading state changed: $isLoading")
    }

    // Prevent auto-navigation when loading
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Show error state
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (error?.contains("404") == true) "Transaksi tidak ditemukan."
                        else "Struk belum bisa dimuat. Transaksi tetap tersimpan dan bisa dilihat di menu Transaksi.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = AppTheme.typography.paragraph1
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = ::backToMenu
                ) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Detail Pembayaran",
                            style = AppTheme.typography.labelBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = ::backToMenu) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.primary)
        ) {
            // Change first: it's what the cashier hands back next
            val total = transaction?.total_amount?.toDoubleOrNull()?.toLong()
            val change = if (received != null && total != null) received - total else null
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (change != null && change > 0) "Kembalian" else "Lunas",
                    style = AppTheme.typography.paragraph1,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    formatRupiah(((if (change != null && change > 0) change else total) ?: 0L).toDouble().toString()),
                    style = AppTheme.typography.heading2Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (received != null) {
                    Text(
                        "Diterima " + formatRupiah(received.toDouble().toString()),
                        style = AppTheme.typography.paragraph2,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = (-80).dp) // Angkat ke atas supaya kelihatan "numpuk"
                        .zIndex(2f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center // Center content when loading/error
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else if (error != null) {
                        Text(
                            text = "Error: ${error}",
                            color = MaterialTheme.colorScheme.error,
                            style = AppTheme.typography.paragraph1
                        )
                    } else {
                        // Display Transaction Receipt when data is loaded
                        transaction?.let { currentTransaction ->
                            TransactionReceipt(
                                cartItems = currentTransaction.transaction_details?.map {
                                    CartItem(
                                        menuId = it.menu_id ?: 0,
                                        name = it.menu_name ?: "",
                                        quantity = it.quantity ?: 0,
                                        price = (it.menu_price?.toDouble() ?: 0.0).toString(),
                                        photo = null
                                    )
                                } ?: emptyList(),
                                totalBiaya = currentTransaction.total_amount?.toDouble() ?: 0.0,
                                paymentMethod = currentTransaction.payment_method,
                                customerName = currentTransaction.nama_transaksi
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxSize()
//                        .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    ,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {

                }

            }

            // TOMBOL DI LUAR CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = ::downloadReceipt,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && transaction != null // Enable button only when data is loaded
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Unduh Struk",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = ::backToMenu,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Pesanan baru")
                    }
                }
            }
        }
    }
}