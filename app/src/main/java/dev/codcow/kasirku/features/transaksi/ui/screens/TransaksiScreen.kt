package dev.codcow.kasirku.features.transaksi.ui.screens

import CustomBottomNavigation
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem
import dev.codcow.kasirku.core.data.model.transaksi.MenuItemUpdate
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.FilterSideTransaksi
import dev.codcow.kasirku.ui.components.FilterSidebarMenu
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TransaksiScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToPayment: (Int) -> Unit
) {
    val currentSearchQuery by viewModel.currentSearchQuery.collectAsState()
    val currentPaymentMethod by viewModel.currentPaymentMethod.collectAsState()
    val currentStatus by viewModel.currentStatus.collectAsState()
    val isFilterVisible by viewModel.isFilterVisible.collectAsState()
    val transaksiItems by viewModel.transaksiItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var query by remember { mutableStateOf("") }

    val allTransactions by viewModel.allTransactions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()

    var selectedTransactionId by remember { mutableStateOf<Int?>(null) }
    val currentTransaction by viewModel.currentTransaction.collectAsState()

    var showFinishTransactionDialog by remember { mutableStateOf(false) }
    var showAntarTransactionDialog by remember { mutableStateOf(false) }
    var currentSelectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val isPdfLoading by viewModel.isPdfLoading.collectAsState()
    val isPdfSuccess by viewModel.isPdfSuccess.collectAsState()
    val pdfError by viewModel.pdfError.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var isUpdatingStatus by remember { mutableStateOf(false) }

    BackHandler(enabled = isFilterVisible) {
        viewModel.closeFilterSidebar()
    }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            viewModel.fetchAllTransaksiNoFilter()
            delay(800)
            refreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    LaunchedEffect(pdfFile, pdfError, isPdfSuccess) {
        pdfFile?.let { file ->
            try {
                // Check if the transaction list is empty based on the API response message
                // This handles the case where API returns:
                // {"status":true,"code":200,"message":"No transactions found","data":[]}
                if (pdfError?.contains("No transactions found", ignoreCase = true) == true ||
                    pdfError?.contains("transaction not found", ignoreCase = true) == true) {
                    Log.i("PDF", "No transactions to display in PDF, skipping file open")
                    // Reset PDF state without trying to open the file
                    viewModel.resetPdfState()
                    return@let
                }

                // Only proceed if the file exists and has content
                if (file.exists() && file.length() > 0) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(intent)
                } else {
                    Log.e("PDF", "PDF file is empty or does not exist")
                }

                // Reset PDF state after handling
                viewModel.resetPdfState()
            } catch (e: Exception) {
                Log.e("PDF", "Failed to open PDF: ${e.message}")
                viewModel.resetPdfState()
            }
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.fetchAllTransaksiNoFilter()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllTransaksiNoFilter()
    }

    LaunchedEffect(isLoading) {
        refreshing = isLoading
        if (!isLoading) {
            delay(800)
            refreshing = false
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Log.e("TransaksiScreen", "Error: $it")
        }
    }

    LaunchedEffect(isUpdatingStatus) {
        if (isUpdatingStatus) {
            viewModel.fetchAllTransaksiNoFilter()
            isUpdatingStatus = false
        }
    }

    if (showFinishTransactionDialog && currentSelectedTransaction != null) {
        if (currentSelectedTransaction?.status == "pending") {
            AlertDialog(
                onDismissRequest = { showFinishTransactionDialog = false },
                containerColor = AppTheme.colors.onSurface,
                textContentColor = AppTheme.colors.surface,
                title = {
                    Text(
                        "Konfirmasi Pembayaran",
                        style = AppTheme.typography.heading5Bold,
                        color = AppTheme.colors.surface
                    )
                },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menyelesaikan transaksi ini? Pastikan pesanan telah sesuai.",
                        style = AppTheme.typography.paragraph2
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            currentSelectedTransaction?.let { transaction ->
                                viewModel.getTransactionById(transaction.id)

                                Log.d("TRANSACTION_DETAILS", "Details: ${transaction.transaction_details}")
                                viewModel.updateStatus(
                                    id = transaction.id,
                                    status = "lunas",
                                    paymentMethod = transaction.payment_method,
                                    is_delivered = transaction.is_delivered
                                )
                                isUpdatingStatus = true
                            }
                            showFinishTransactionDialog = false
                        }
                    ) {
                        Text(
                            "Ya",
                            style = AppTheme.typography.paragraph2Bold,
                            color = AppTheme.colors.onHighlightSurface
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishTransactionDialog = false }) {
                        Text(
                            "Tidak",
                            style = AppTheme.typography.paragraph2Bold,
                            color = AppTheme.colors.surface
                        )
                    }
                }
            )
        }
    }

    if (showAntarTransactionDialog && currentSelectedTransaction != null) {
        if (currentSelectedTransaction?.is_delivered == "no") {
            AlertDialog(
                onDismissRequest = { showAntarTransactionDialog = false },
                containerColor = AppTheme.colors.onSurface,
                textContentColor = AppTheme.colors.surface,
                title = {
                    Text(
                        "Tandai sebagai diantar",
                        style = AppTheme.typography.heading5Bold,
                        color = AppTheme.colors.surface
                    )
                },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menyelesaikan transaksi ini? Pastikan pesanan telah diterima oleh pelanggan.",
                        style = AppTheme.typography.paragraph2
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            currentSelectedTransaction?.let { transaction ->
                                viewModel.getTransactionById(transaction.id)

                                Log.d("TRANSACTION_DETAILS", "Details: ${transaction.transaction_details}")
                                viewModel.updateStatus(
                                    id = transaction.id,
                                    status = transaction.status,
                                    paymentMethod = transaction.payment_method,
                                    is_delivered = "yes"
                                )
                                isUpdatingStatus = true
                            }
                            showAntarTransactionDialog = false
                        }
                    ) {
                        Text(
                            "Ya",
                            style = AppTheme.typography.paragraph2Bold,
                            color = AppTheme.colors.onHighlightSurface
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAntarTransactionDialog = false }) {
                        Text(
                            "Tidak",
                            style = AppTheme.typography.paragraph2Bold,
                            color = AppTheme.colors.surface
                        )
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .weight(1f)
                    .background(Color(0xFFFFFFFF)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Transaksi",
                    style = AppTheme.typography.labelBold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp)
                ) {
                    SearchBarMenu(
                        query = query,
                        onQueryChange = { newQuery ->
                            Log.d("SearchDebug", "Query Changed: $newQuery")
                            query = newQuery
                            viewModel.updateSearchQuery(newQuery)
                            if (newQuery.isEmpty() && currentPaymentMethod.isEmpty() && currentStatus.isEmpty()) {
                                viewModel.fetchAllTransaksiNoFilter()
                            } else if (query.isEmpty()) {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
                                viewModel.fetchAllTransaksiAcrossPages(currentPaymentMethod, currentStatus, today, today)
                            } else {
                                viewModel.searchTransactions(newQuery, currentPaymentMethod, currentStatus)
                            }
                        },
                        placeholder = "Cari Nama Transaksi",
                        onFilterClick = { viewModel.toggleFilterSidebar() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pullRefresh(pullRefreshState)
                                .padding(horizontal = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {

                        }
                    } else if (error != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                                .pullRefresh(pullRefreshState),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$error",
                                style = AppTheme.typography.paragraph1,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, end = 16.dp)
                        ) {
                            items(allTransactions.filter {
                                it.nama_transaksi?.contains(query, ignoreCase = true) ?: false
                            }) { transaction ->
                                TransaksiItemTransaksi(
                                    transaction = transaction,
                                    onFinishTransaction = { selectedTransaction ->
                                        // Ubah logika disini
                                        if (selectedTransaction.status == "pending") {
                                            // Ambil data transaksi terlebih dahulu
                                            viewModel.getTransactionById(selectedTransaction.id)

                                            // Navigasi ke halaman pembayaran untuk update payment method
                                            Log.d("TransaksiScreen", "Navigating to payment for transaction ID: ${selectedTransaction.id}")
                                            onNavigateToPayment(selectedTransaction.id)
                                        } else {
                                            // Untuk transaksi non-pending, tetap gunakan dialog konfirmasi lama
                                            showFinishTransactionDialog = true
                                            currentSelectedTransaction = selectedTransaction
                                        }
                                    },
                                    onItemClick = { clickedTransaction ->
                                        onNavigateToDetail(clickedTransaction.id ?: -1)
                                    },
                                    onAntarTransaction = { selectedTransaction ->
                                        if (selectedTransaction.is_delivered == "no") {
                                            showAntarTransactionDialog = true
                                            currentSelectedTransaction = selectedTransaction
                                        }

                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    // Place the PullRefreshIndicator inside the Box that has pullRefresh modifier
                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = AppTheme.colors.surface,
                        contentColor = Color.White
                    )
                }
            }

            // Bottom navigation stays outside the refreshable content
            CustomBottomNavigation(
                currentRoute = currentRoute(navController = navController),
                onNavigateToHome = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                },
                onNavigateToRekap = {
                    navController.navigate(Screen.Rekap.route) {
                        popUpTo(Screen.Rekap.route) { inclusive = true }
                    }
                },
                onNavigateToTransaksi = {
                    navController.navigate(Screen.Transaksi.route) {
                        popUpTo(Screen.Transaksi.route) { inclusive = true }
                    }
                }
            )
        }

        // Filter sidebar should be outside the main layout
        Box(modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd)) {
            FilterSideTransaksi(
                isVisible = isFilterVisible,
                onDismiss = { viewModel.closeFilterSidebar() },
                searchQuery = query,
                onResetFilter = { viewModel.fetchAllTransaksiNoFilter() },
                onApplyFilter = { query, paymentMethod, status ->
                    if (query.isEmpty() && paymentMethod.isEmpty() && status.isEmpty()) {
                        viewModel.fetchAllTransaksiNoFilter()
                    } else if (query.isEmpty()) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
                        viewModel.fetchAllTransaksiAcrossPages(paymentMethod, status, today, today)
                    } else {
                        viewModel.searchTransactions(query, paymentMethod, status)
                    }
                },
                onDownloadPdf = { status, paymentMethod ->
                    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val currentTime = dateFormat.format(Date())
                    val fileName = "transaksi_${currentTime}"
                    viewModel.downloadTransaksiPdf(status, paymentMethod, fileName)
                },
                isPdfLoading = isPdfLoading,
                isPdfSuccess = isPdfSuccess,
                pdfError = pdfError
            )
        }
    }
}