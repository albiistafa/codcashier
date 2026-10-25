package dev.codcow.kasirku.features.transaksi.ui.screens
import dev.codcow.kasirku.ui.components.readableWidth
import dev.codcow.kasirku.ui.components.ErrorState
import androidx.compose.foundation.lazy.itemsIndexed
import dev.codcow.kasirku.ui.components.ConfirmDialog
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import dev.codcow.kasirku.ui.components.GreenBand
import dev.codcow.kasirku.ui.components.SearchPill
import dev.codcow.kasirku.ui.components.fullBleed

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import CustomBottomNavigation
import android.R
import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.components.TransaksiItemDelete
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import dev.codcow.kasirku.ui.components.TransaksiItemPengeluaran

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiManager(
    navController: NavController,
    viewModel: TransaksiViewModel = hiltViewModel(),
    onEditClick: (Int) -> Unit = {}
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allPengeluaran by viewModel.allPengeluaran.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val currentStatus by viewModel.currentStatus.collectAsState()
    val currentPaymentMethod by viewModel.currentPaymentMethod.collectAsState()
    var activeFilter by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeletePeriodeDialog by remember { mutableStateOf(false) }
    var selectedTransaksiId by remember { mutableStateOf<Int?>(null) }
    var isDeletePengeluaran by remember { mutableStateOf(false) }

    var query by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var isPendingActive by remember { mutableStateOf(false) }
    var isLunasActive by remember { mutableStateOf(false) }
    var isDateActive by remember { mutableStateOf(false) }



    fun refreshData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        viewModel.fetchAllPengeluaranAcrossPages(today, today)
        viewModel.fetchAllTransaksiNoFilter()
    }

    // Debug logging
    LaunchedEffect(allTransactions) {
        Log.d("TransaksiManager", "AllTransactions: ${allTransactions.size} items")
    }

    LaunchedEffect(allPengeluaran) {
        Log.d("TransaksiManager", "AllPengeluaran: ${allPengeluaran.size} items")
    }

    // Initial fetch
    LaunchedEffect(Unit) {
        Log.d("TransaksiManager", "Initial fetch started")
        refreshData()
    }

    // Handle loading state
    LaunchedEffect(isLoading) {
        refreshing = isLoading
        if (!isLoading) {
            delay(500)
            refreshing = false
        }
    }

    // Handle success state with proper refresh
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            refreshData()
            viewModel.resetState()
        }
    }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            refreshData()
            delay(500)
            refreshing = false
        }
    }

    var startDate: LocalDate? by remember { mutableStateOf(null) }
    var endDate: LocalDate? by remember { mutableStateOf(null) }

    var tanggalMulaiText by remember { mutableStateOf("") }
    var tanggalSelesaiText by remember { mutableStateOf("") }

    val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun applyCombinedFilters() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        // Determine status filter
        val status = when {
            isPendingActive && !isLunasActive -> "pending"
            isLunasActive && !isPendingActive -> "lunas"
            else -> "" // Both active or both inactive = no status filter
        }

        // Determine date range
        val (startDateStr, endDateStr) = if (isDateActive && startDate != null && endDate != null) {
            startDate!!.format(apiFormatter) to endDate!!.format(apiFormatter)
        } else {
            today to today
        }

        // Apply filters
        viewModel.fetchAllTransaksiAcrossPages("", status, startDateStr, endDateStr)
        viewModel.fetchAllPengeluaranAcrossPages(startDateStr, endDateStr)
    }

    fun applyDateFilter() {
        if (startDate != null && endDate != null) {
            isDateActive = true
            applyCombinedFilters()
        }
    }

    fun resetDateFilter() {
        startDate = null
        endDate = null
        tanggalMulaiText = ""
        tanggalSelesaiText = ""
        isDateActive = false

        // Apply combined filters (which will use today's date since isDateActive is false)
        applyCombinedFilters()
    }

    fun showDatePicker(isStartDate: Boolean) {
        val initialDate = if (isStartDate) startDate else endDate
        val year = initialDate?.year ?: calendar.get(Calendar.YEAR)
        val month = initialDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
        val day = initialDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                if (isStartDate) {
                    startDate = selectedDate
                    tanggalMulaiText = selectedDate.format(displayFormatter)
                } else {
                    endDate = selectedDate
                    tanggalSelesaiText = selectedDate.format(displayFormatter)
                }
            },
            year,
            month,
            day
        ).show()
    }

    fun showDatePickerFilter(isStartDate: Boolean) {
        val initialDate = if (isStartDate) startDate else endDate
        val year = initialDate?.year ?: calendar.get(Calendar.YEAR)
        val month = initialDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
        val day = initialDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                if (isStartDate) {
                    startDate = selectedDate
                    tanggalMulaiText = selectedDate.format(displayFormatter)

                    if (endDate == null) {
                        showDatePicker(false)
                    } else {
                        applyDateFilter()
                    }
                } else {
                    endDate = selectedDate
                    tanggalSelesaiText = selectedDate.format(displayFormatter)
                    applyDateFilter()
                }
            },
            year,
            month,
            day
        ).show()
    }



    val pullRefreshState = rememberPullToRefreshState()

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus transaksi?",
            message = "Transaksi \u201C${allTransactions.find { it.id == selectedTransaksiId }?.nama_transaksi ?: "ini"}\u201D akan dihapus dan tidak bisa dikembalikan.",
            confirmLabel = "Hapus transaksi",
            destructive = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                        selectedTransaksiId?.let { id ->
                            viewModel.deleteTransaksi(id)
                            if (isDeletePengeluaran) {
                                // Immediately refresh the pengeluaran list when deleting a pengeluaran
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
                                viewModel.fetchAllPengeluaranAcrossPages(today, today)
                            } else {
                                // Immediately refresh the transaction list when deleting a transaction
                                viewModel.fetchAllTransaksiNoFilter()
                            }
                        }
                        showDeleteDialog = false
            }
        )
    }

    if (showDeletePeriodeDialog) {
        ConfirmDialog(
            title = "Hapus transaksi satu periode?",
            message = "Semua transaksi di antara dua tanggal ini akan dihapus dan tidak bisa dikembalikan.",
            confirmLabel = "Hapus periode",
            destructive = true,
            onDismiss = { showDeletePeriodeDialog = false },
            onConfirm = {
                        val startDateString = startDate?.let {
                            if (it != LocalDate.MIN) it.format(apiFormatter) else ""
                        } ?: ""
                        val endDateString = endDate?.let {
                            if (it != LocalDate.MIN) it.format(apiFormatter) else ""
                        } ?: ""

                        viewModel.deletePeriode(startDateString, endDateString)
                        showDeletePeriodeDialog = false
            },
            content = {
                Column {
                    OutlinedTextField(
                        value = tanggalMulaiText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal Mulai", style = AppTheme.typography.paragraph2) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker(true) }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick Start Date")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tanggalSelesaiText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal Akhir", style = AppTheme.typography.paragraph2) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker(false) }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick End Date")
                            }
                        }
                    )
                }
            }
        )
    }

    if (showDatePickerDialog) {
        Dialog(
            onDismissRequest = { showDatePickerDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Pilih Rentang Tanggal",
                        style = AppTheme.typography.heading5Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = tanggalMulaiText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal Mulai", style = AppTheme.typography.paragraph2) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerFilter(true) }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick Start Date")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tanggalSelesaiText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal Akhir", style = AppTheme.typography.paragraph2) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerFilter(false) }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick End Date")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDatePickerDialog = false }
                        ) {
                            Text(
                                "Batal",
                                style = AppTheme.typography.paragraph2Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                if (startDate != null && endDate != null) {
                                    applyDateFilter()
                                    showDatePickerDialog = false
                                }
                            },
                            enabled = startDate != null && endDate != null
                        ) {
                            Text(
                                "Terapkan",
                                style = AppTheme.typography.paragraph2Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues) // Scaffold insets include the status bar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                GreenBand(
                    title = "Kelola transaksi",
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.fullBleed(20.dp).background(AppTheme.colors.hero.first()).statusBarsPadding()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isPendingActive,
                        onClick = {
                            isPendingActive = !isPendingActive
                            applyCombinedFilters()
                        },
                        label = {
                            Text(
                                "Belum dibayar",
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    FilterChip(
                        selected = isLunasActive,
                        onClick = {
                            isLunasActive = !isLunasActive
                            applyCombinedFilters()
                        },
                        label = {
                            Text(
                                "Lunas",
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    FilterChip(
                        selected = isDateActive,
                        onClick = {
                            if (isDateActive) {
                                resetDateFilter()
                            } else {
                                showDatePickerDialog = true
                            }
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Tanggal",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDateActive && startDate != null && endDate != null) {
                                       "Tanggal"
                                    } else {
                                        "Tanggal"
                                    }
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                        }
                    } else if (error != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // "Tidak ada data transaksi" is the API's empty answer, not a failure
                            if (error == "Tidak ada data transaksi") {
                                Text("Belum ada transaksi", style = AppTheme.typography.paragraph1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                ErrorState(message = error.orEmpty(), onRetry = ::refreshData)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.readableWidth()
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                            contentPadding = PaddingValues(bottom = 24.dp, top = 10.dp)
                        ) {
                            if (allTransactions.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Transaksi",
                                        style = AppTheme.typography.paragraph2Semibold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                itemsIndexed(allTransactions) { i, transaksi ->
                                    TransaksiItemDelete(
                                        transaksi = transaksi,
                                        index = i,
                                        count = allTransactions.size,
                                        onDeleteClick = { id ->
                                            selectedTransaksiId = id
                                            isDeletePengeluaran = false  // Mark as regular transaction deletion
                                            showDeleteDialog = true
                                        },
                                        onItemClick = { id ->
                                            if (transaksi.status == "pending"){
                                                navController.navigate(Screen.Detail.createRoute(id))
                                            } else {

                                            }
//                                            navController.navigate(Screen.Detail.createRoute(id))
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            if (allPengeluaran.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Pengeluaran",
                                        style = AppTheme.typography.paragraph2Semibold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                itemsIndexed(allPengeluaran) { i, pengeluaran ->
                                    TransaksiItemPengeluaran(
                                        pengeluaran = pengeluaran,
                                        index = i,
                                        count = allPengeluaran.size,
                                        onDeleteClick = { id ->
                                            selectedTransaksiId = id
                                            isDeletePengeluaran = true  // Mark as pengeluaran deletion
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }

                            // Deleting a whole period is rare and irreversible: a plain red link at the end, not a floating button
                            item {
                                TextButton(
                                    onClick = { showDeletePeriodeDialog = true },
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Hapus transaksi satu periode…", style = AppTheme.typography.paragraph2Semibold, color = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Only show "No transactions" if both lists are empty
                            if (allTransactions.isEmpty() && allPengeluaran.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Tidak ada transaksi",
                                            style = AppTheme.typography.paragraph1,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
}





