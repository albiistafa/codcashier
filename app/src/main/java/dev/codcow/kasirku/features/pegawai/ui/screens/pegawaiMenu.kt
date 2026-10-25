package dev.codcow.kasirku.features.pegawai.ui.screens
import dev.codcow.kasirku.ui.components.readableWidth
import dev.codcow.kasirku.ui.components.ListHint
import dev.codcow.kasirku.ui.components.AddFab
import androidx.compose.foundation.lazy.itemsIndexed
import dev.codcow.kasirku.ui.components.ConfirmDialog
import dev.codcow.kasirku.ui.components.ErrorState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel
import dev.codcow.kasirku.ui.components.KategoriListItem
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.PegawaiItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PegawaiManager(
    navController: NavController,
    viewModel: PegawaiManagerViewModel = hiltViewModel(),
    onEditClick: (Int) -> Unit = {}
) {
    val pegawaiItems by viewModel.pegawaiItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var query by remember { mutableStateOf("") }

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedPegawaiId by remember { mutableStateOf<Int?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus pegawai?",
            message = "Akun pegawai \u201C${pegawaiItems.find { it.id == selectedPegawaiId }?.name.orEmpty()}\u201D akan dihapus dan tidak bisa dikembalikan.",
            confirmLabel = "Hapus pegawai",
            destructive = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                        selectedPegawaiId?.let { id ->
                            viewModel.deletePegawaiItem(id)
                        }
                        showDeleteDialog = false
            }
        )
    }


    LaunchedEffect(Unit) {
        viewModel.fetchPegawaiItems()
    }

    LaunchedEffect(isLoading) { // Observe perubahan pada isLoading
        refreshing = isLoading // Set refreshing true saat isLoading true
        if (!isLoading) {
            delay(800) // Optional delay untuk visual pull-to-refresh
            refreshing = false
        }
    }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            viewModel.fetchPegawaiItems()
            delay(800)
            refreshing = false
        }
    }

    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AddFab("Tambah pegawai") { navController.navigate("create_pegawai") }
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
                    title = "Pegawai",
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
                            viewModel.fetchPegawaiItems() // Panggil fetchMenuItems() hanya jika newQuery benar-benar kosong
                        } else {
                            query = newQuery
                            viewModel.searchPegawaiItems(newQuery)
                        }
                    },
                    placeholder = "Cari pegawai"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
                ) {
                    LazyColumn(
                        modifier = Modifier.readableWidth().fillMaxSize(),
                                                contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp)
                    ) {
                        when {
                            isLoading -> item {

                            }
                            error != null -> item {
                                ErrorState(message = error ?: "", onRetry = { viewModel.fetchPegawaiItems() })
                            }
                            else -> {
                                item { ListHint("Pegawai bisa membuka Kasir, Transaksi, dan Rekap") }
itemsIndexed(pegawaiItems, key = { _, it -> it.id }) { i, pegawai ->
                                    PegawaiItem(
                                        index = i,
                                        count = pegawaiItems.size,
                                        pegawai = pegawai,
                                        onDeleteClick = { id ->
                                            selectedPegawaiId = id
                                            showDeleteDialog = true }
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
                }
            }
        }
    }
}