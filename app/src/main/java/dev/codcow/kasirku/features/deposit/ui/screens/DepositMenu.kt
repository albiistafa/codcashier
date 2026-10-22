package dev.codcow.kasirku.features.deposit.ui.screens
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
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalMaterial3Api::class)
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
        ConfirmDialog(
            title = "Hapus pelanggan deposit?",
            message = "Data deposit \u201C${depositItems.find { it.id == selectedDepositId }?.customer_name.orEmpty()}\u201D akan dihapus dan tidak bisa dikembalikan.",
            confirmLabel = "Hapus deposit",
            destructive = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                        selectedDepositId?.let { id ->
                            viewModel.deleteDeposit(id)
                        }
                        showDeleteDialog = false
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

    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CustomBottomNavigation(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_deposit") },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Deposit",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues) // Scaffold insets include the status bar
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                GreenBand(
                    title = "Deposit",
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.fullBleed(20.dp).background(AppTheme.colors.hero.first()).statusBarsPadding()
                ) {
                    SearchPill(query = query,
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
                    placeholder = "Cari pelanggan deposit"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
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
                                items(depositItems, key = { it.id }) { deposit ->
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

                    PullToRefreshDefaults.Indicator(
                        isRefreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaterialTheme.colorScheme.primary,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    if (showTopUpSheet && selectedDeposit != null) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showTopUpSheet = false
                                selectedDeposit = null
                            },
                            sheetState = bottomSheetState,
                            containerColor = MaterialTheme.colorScheme.surface
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
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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