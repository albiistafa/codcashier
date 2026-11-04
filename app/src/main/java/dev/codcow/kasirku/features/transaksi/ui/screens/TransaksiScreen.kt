package dev.codcow.kasirku.features.transaksi.ui.screens
import dev.codcow.kasirku.ui.components.ConfirmDialog

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import CustomBottomNavigation
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.ui.components.BandPill
import dev.codcow.kasirku.ui.components.GreenBand
import dev.codcow.kasirku.ui.components.SearchPill
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem
import dev.codcow.kasirku.core.data.model.transaksi.MenuItemUpdate
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.middleware.Screen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToPayment: (Int) -> Unit
) {
    val transaksiItems by viewModel.transaksiItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var query by remember { mutableStateOf("") }

    val allTransactions by viewModel.allTransactions.collectAsState()

    var selectedTransactionId by remember { mutableStateOf<Int?>(null) }
    val currentTransaction by viewModel.currentTransaction.collectAsState()

    var showFinishTransactionDialog by remember { mutableStateOf(false) }
    var showAntarTransactionDialog by remember { mutableStateOf(false) }
    var currentSelectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val pdf by viewModel.pdf.collectAsState()
    val isPdfLoading = pdf == TransaksiViewModel.PdfState.Loading

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    var isUpdatingStatus by remember { mutableStateOf(false) }


    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            viewModel.fetchAllTransaksiNoFilter()
            delay(800)
            refreshing = false
        }
    }

    val pullRefreshState = rememberPullToRefreshState()

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
            ConfirmDialog(
                title = "Tandai lunas?",
                message = "Pesanan \u201C${currentSelectedTransaction?.nama_transaksi.orEmpty()}\u201D ditandai lunas. Pastikan uangnya sudah diterima.",
                confirmLabel = "Tandai lunas",
                destructive = false,
                onDismiss = { showFinishTransactionDialog = false },
                onConfirm = {
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
            )
        }
    }

    if (showAntarTransactionDialog && currentSelectedTransaction != null) {
        if (currentSelectedTransaction?.is_delivered == "no") {
            ConfirmDialog(
                title = "Tandai sudah diantar?",
                message = "Pesanan \u201C${currentSelectedTransaction?.nama_transaksi.orEmpty()}\u201D ditandai sudah diterima pelanggan.",
                confirmLabel = "Sudah diantar",
                destructive = false,
                onDismiss = { showAntarTransactionDialog = false },
                onConfirm = {
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
            )
        }
    }

    // Same filters as the old side panel: one status, one payment method, tap again to clear
    var status by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }
    val pendingCount by viewModel.pendingCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.refreshPendingCount() }
    PdfStateEffect(pdf, snackbarHostState, viewModel::resetPdfState)

    // Typing filters the loaded list on the phone (visibleTransactions); only the chips go to the server
    fun applyFilter() {
        if (method.isEmpty() && status.isEmpty()) {
            viewModel.fetchAllTransaksiNoFilter()
        } else {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            viewModel.fetchAllTransaksiAcrossPages(method, status, today, today)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { CustomBottomNavigation(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GreenBand(
                title = "Transaksi",
                modifier = Modifier.background(AppTheme.colors.hero.first()).statusBarsPadding(),
                actions = {
                    BandPill(onClick = {
                        val fileName = "transaksi_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        viewModel.downloadTransaksiPdf(status, method, fileName)
                    }, enabled = !isPdfLoading) {
                        if (isPdfLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AppTheme.colors.onHero)
                        else Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Unduh PDF", style = AppTheme.typography.paragraph2Semibold)
                    }
                }
            ) {
                SearchPill(query, { q -> query = q }, "Cari nama transaksi")
            }
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterRow("Status", listOf("pending" to "Belum dibayar", "lunas" to "Lunas"), status, badge = { if (it == "pending") pendingCount else 0 }) {
                    status = if (status == it) "" else it; applyFilter()
                }
                FilterRow("Bayar", listOf("tunai" to "Tunai", "digital" to "Digital", "deposit" to "Deposit"), method) {
                    method = if (method == it) "" else it; applyFilter()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
            ) {
                if (error != null && !isLoading) {
                    Text(
                        text = if (error == "Tidak ada data transaksi") "Belum ada transaksi" else error.orEmpty(),
                        style = AppTheme.typography.paragraph1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp, start = 20.dp, end = 20.dp)
                    )
                } else if (!isLoading) {
                    val visibleTransactions = allTransactions.filter {
                        it.nama_transaksi?.contains(query, ignoreCase = true) ?: false
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
                    ) {
                        if (visibleTransactions.isEmpty()) {
                            item {
                                Text(
                                    text = if (query.isBlank()) "Belum ada transaksi" else "Tidak ada transaksi untuk \"$query\"",
                                    style = AppTheme.typography.paragraph1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                                )
                            }
                        }
                        itemsIndexed(visibleTransactions, key = { _, t -> t.id }) { i, transaction ->
                            // A finished order slides out and the rest close the gap instead of jumping
                            Box(Modifier.animateItem()) { TransaksiItemTransaksi(
                                transaction = transaction,
                                divider = i < visibleTransactions.lastIndex,
                                onFinishTransaction = { selectedTransaction ->
                                    if (selectedTransaction.status == "pending") {
                                        // Load the order first, then pay it on the payment screen
                                        viewModel.getTransactionById(selectedTransaction.id)
                                        onNavigateToPayment(selectedTransaction.id)
                                    } else {
                                        showFinishTransactionDialog = true
                                        currentSelectedTransaction = selectedTransaction
                                    }
                                },
                                onItemClick = { clicked -> onNavigateToDetail(clicked.id) },
                                onAntarTransaction = { selectedTransaction ->
                                    if (selectedTransaction.is_delivered == "no") {
                                        showAntarTransactionDialog = true
                                        currentSelectedTransaction = selectedTransaction
                                    }
                                }
                            ) }
                        }
                    }
                }
                PullToRefreshDefaults.Indicator(
                    isRefreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.primary,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/** One filter group as chips: picking the selected chip again clears it. */
@Composable
private fun FilterRow(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    badge: (String) -> Int = { 0 },
    onPick: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = AppTheme.typography.paragraph2Semibold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(48.dp))
        options.forEach { (value, text) ->
            val on = selected == value
            FilterChip(
                selected = on,
                onClick = { onPick(value) },
                label = { Text(text, style = AppTheme.typography.paragraph2Semibold) },
                trailingIcon = badge(value).takeIf { it > 0 }?.let { n ->
                    {
                        Box(Modifier.defaultMinSize(20.dp, 20.dp).background(AppTheme.colors.money, CircleShape).padding(horizontal = 5.dp), contentAlignment = Alignment.Center) {
                            Text("$n", color = AppTheme.colors.onMoney, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppTheme.colors.deep,
                    selectedLabelColor = AppTheme.colors.onDeep,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = on,
                    borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = AppTheme.colors.deep
                ),
                modifier = Modifier.height(40.dp)
            )
        }
    }
}
