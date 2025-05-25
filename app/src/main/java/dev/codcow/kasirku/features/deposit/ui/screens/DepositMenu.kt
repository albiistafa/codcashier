package dev.codcow.kasirku.features.deposit.ui.screens

import CustomBottomNavigation
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel
import dev.codcow.kasirku.ui.components.KategoriListItem
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.DepositItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DepositManager(
    navController: NavController,
    viewModel: DepositViewModel = hiltViewModel()
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTopUpSheet by remember { mutableStateOf(false) }
    var selectedDeposit by remember { mutableStateOf<DataDeposit?>(null) }

    var amountText by remember { mutableStateOf("") }
    val isAmountValid = amountText.toIntOrNull() != null && amountText.toInt() > 0

    val depositItems by viewModel.depositItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var query by remember { mutableStateOf("") }

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedDepositId by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppTheme.colors.onSurface,
            textContentColor = AppTheme.colors.surface,
            title = { Text("Konfirmasi Hapus", style = AppTheme.typography.heading5Bold,
                color = AppTheme.colors.surface) },
            text = { Text("Apakah Anda yakin ingin menghapus deposit ini?", style = AppTheme.typography.paragraph2) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDepositId?.let { id ->
                            viewModel.deleteDeposit(id)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Ya", style = AppTheme.typography.paragraph2Bold,
                        color = AppTheme.colors.onHighlightSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Tidak", style = AppTheme.typography.paragraph2Bold,
                        color = AppTheme.colors.surface)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.getAllDeposits()
    }

    LaunchedEffect(isLoading) {
        refreshing = isLoading
        if (!isLoading) {
            delay(800)
            refreshing = false
        }
    }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            delay(800)
            refreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(
                currentRoute = currentRoute(navController = navController),
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToRekap = {
                    navController.navigate(Screen.Rekap.route) {
                        popUpTo(Screen.Rekap.route) { inclusive = true }
                    }
                },
                onNavigateToTransaksi = {
                    navController.navigate(Screen.Transaksi.route) {
                        popUpTo(Screen.Transaksi.route) { inclusive = true}
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_deposit") },
                shape = CircleShape,
                containerColor = AppTheme.colors.surface,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Deposit",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.onSurface)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp, vertical = 30.dp)
            ) {
                WelcomeTopBar(navController = navController)
                Spacer(modifier = Modifier.height(14.dp))

                SearchBar(
                    query = query,
                    onQueryChange = { newQuery ->
                        // Tambahkan log untuk debugging
                        Log.d("SearchDebug", "Query Changed: $newQuery")
                        // Cek apakah string pencarian benar-benar kosong
                        if (newQuery.isEmpty()) {
                            query = newQuery
                            viewModel.getAllDeposits() // Panggil fetchMenuItems() hanya jika newQuery benar-benar kosong
                        } else {
                            query = newQuery
                            viewModel.searchDepositItems(newQuery)
                        }
                    },
                    placeholder = "Cari Deposit"
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp)
                    ) {
                        when {
                            isLoading -> item {

                            }

                            else -> {
                                items(depositItems) { deposit ->
                                    DepositItem(
                                        deposit = deposit,
                                        onEditClick = {
                                            selectedDeposit = deposit
                                            showTopUpSheet = true},
                                        onDeleteClick = {
                                                id ->
                                            selectedDepositId = deposit.customer_id
                                            showDeleteDialog = true
                                        },
                                        onItemClick = {navController.navigate(Screen.EditDeposit.createRoute(deposit.customer_id))}
                                    )
                                }
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = AppTheme.colors.surface,
                        contentColor = Color.White
                    )

                    if (showTopUpSheet && selectedDeposit != null) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showTopUpSheet = false
                                selectedDeposit = null
                            },
                            sheetState = bottomSheetState,
                            containerColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Top Up untuk: ${selectedDeposit!!.customer_name}",
                                    style = AppTheme.typography.paragraph1Semibold)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { newText ->
                                        // Hanya izinkan angka saja
                                        if (newText.all { it.isDigit() }) {
                                            amountText = newText
                                        }
                                    },
                                    label = { Text("Jumlah Top Up") },
                                    leadingIcon = { Text("Rp", modifier = Modifier.padding(start = 8.dp)) },
                                    placeholder = { Text("Masukkan nominal dalam angka") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = AppTheme.colors.surface,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val amount = amountText.toIntOrNull() ?: 0
                                        viewModel.topUpBalance(id = selectedDeposit!!.customer_id, amount = amount)
                                        showTopUpSheet = false
                                        selectedDeposit = null
                                        amountText = ""
                                    },
                                    enabled = isAmountValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.surface,
                                        disabledContainerColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Konfirmasi Top Up")
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}