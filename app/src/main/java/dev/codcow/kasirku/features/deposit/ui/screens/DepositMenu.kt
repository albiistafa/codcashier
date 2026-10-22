package dev.codcow.kasirku.features.deposit.ui.screens
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import dev.codcow.kasirku.ui.util.formatRupiah
import dev.codcow.kasirku.ui.components.InitialsAvatar
import dev.codcow.kasirku.ui.components.ListHint
import dev.codcow.kasirku.ui.components.AddFab
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
        floatingActionButton = {
            AddFab("Tambah pelanggan") { navController.navigate("create_deposit") }
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
                                                contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp)
                    ) {
                        when {
                            isLoading -> item {

                            }

                            else -> {
                                item { ListHint("Ketuk pelanggan untuk mengubah data atau menghapusnya") }
itemsIndexed(depositItems, key = { _, it -> it.id }) { i, deposit ->
                                    DepositItem(
                                        index = i,
                                        count = depositItems.size,
                                        deposit = deposit,
                                        onTopUpClick = {
                                            selectedDeposit = deposit
                                            showTopUpSheet = true
                                        },
                                        onItemClick = { navController.navigate(Screen.EditDeposit.createRoute(deposit.customer_id)) }
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
                            ){
                                val d = selectedDeposit!!
                                val now = d.balance.toDoubleOrNull() ?: 0.0
                                val add = amountText.toLongOrNull() ?: 0L
                                fun rp(v: Double) = formatRupiah(v.toString()).replace("Rp ", "Rp")
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    InitialsAvatar(d.customer_name)
                                    Column {
                                        Text("Isi saldo ${d.customer_name}", style = AppTheme.typography.heading5Bold)
                                        Text("Saldo sekarang ${rp(now)}", style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { newText -> if (newText.all { it.isDigit() } && newText.length <= 9) amountText = newText },
                                    label = { Text("Jumlah isi saldo") },
                                    prefix = { Text("Rp") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = AppTheme.typography.heading5Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Text("Saldo setelah diisi", style = AppTheme.typography.paragraph1Semibold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                                    Text(rp(now + add), style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.topUpBalance(id = d.customer_id, amount = add.toInt())
                                        showTopUpSheet = false
                                        selectedDeposit = null
                                        amountText = ""
                                    },
                                    enabled = isAmountValid,
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.money,
                                        contentColor = AppTheme.colors.onMoney,
                                        disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                ) {
                                    Text(if (add > 0) "Isi saldo ${rp(add.toDouble())}" else "Isi jumlahnya dulu", style = AppTheme.typography.paragraph1Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                }
            }
        }
    }
}