package dev.codcow.kasirku.features.kategoriManager.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.ui.components.KategoriListItem
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.middleware.Screen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun KategoriManager(
    navController: NavController,
    viewModel: KategoriManagerViewModel = hiltViewModel(),
    onEditClick: (Int) -> Unit = {}
) {
    val kategoriItems by viewModel.kategoriItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var query by remember { mutableStateOf("") }

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedKategoriId by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppTheme.colors.onSurface,
            textContentColor = AppTheme.colors.surface,
            title = { Text("Konfirmasi Hapus", style = AppTheme.typography.heading5Bold,
                color = AppTheme.colors.surface) },
            text = { Text("Apakah Anda yakin ingin menghapus kategori ini?", style = AppTheme.typography.paragraph2) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedKategoriId?.let { id ->
                            viewModel.deleteKategoriItem(id)
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
        viewModel.fetchKategoriItems()
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
            viewModel.fetchKategoriItems()
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
                        popUpTo(Screen.Transaksi.route) { inclusive = true}
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_kategori") },
                shape = CircleShape,
                containerColor = AppTheme.colors.surface,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Kategori",
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
                            viewModel.fetchKategoriItems() // Panggil fetchMenuItems() hanya jika newQuery benar-benar kosong
                        } else {
                            query = newQuery
                            viewModel.searchKategoriItems(newQuery)
                        }
                    },
                    placeholder = "Cari Kategori"
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
                            error != null -> item {
                                Text(
                                    text = "Error: $error",
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            else -> {
                                items(kategoriItems) { kategori ->
                                    KategoriListItem(
                                        kategori = kategori,
                                        onEditClick = { id ->
                                            navController.navigate("edit_kategori/$id")
                                        },
                                        onDeleteClick = { id ->
                                            selectedKategoriId = id
                                            showDeleteDialog = true }
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
                }
            }
        }
    }
}