package dev.codcow.kasirku.features.menuManager.ui.screens

import CustomBottomNavigation
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.FilterSidebarMenu
import dev.codcow.kasirku.ui.components.MenuCard
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MenuManager(
    navController: NavController,
    viewModel: MenuManagerViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {
        navController.navigate(Screen.CreateMenu.route) // Tambahkan ini
    },
    onEditClick: (Int) -> Unit = {}
) {
    var showFilter by remember { mutableStateOf(false) }
    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var query by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val categoryMap = remember(categories) {
        categories.associate { it.id to it.name }
    }

    val subCategoryMap = remember(subCategories) {
        subCategories.associate { it.id to it.name }
    }

    BackHandler(enabled = showFilter) {
        showFilter = false
    }

    var selectedMenuId by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppTheme.colors.onSurface,
            textContentColor = AppTheme.colors.surface,
            title = { Text("Konfirmasi Hapus", style = AppTheme.typography.heading5Bold,
                color = AppTheme.colors.surface) },
            text = { Text("Apakah Anda yakin ingin menghapus menu ini?", style = AppTheme.typography.paragraph2) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMenuId?.let { id ->
                            viewModel.deleteMenuItem(id)
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
        viewModel.fetchMenuItems()
        viewModel.fetchCategories()
        viewModel.fetchSubCategories()
    }

    LaunchedEffect(isLoading) { // Observe perubahan pada isLoading
        refreshing = isLoading
        if (!isLoading) {
            delay(800)
            refreshing = false
        }
    }
    // Function to handle refresh action
    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            // Refresh data from API
            viewModel.fetchCategories()
            viewModel.fetchSubCategories()
            viewModel.fetchMenuItems()
            // Add small delay to show refresh indicator
            delay(800)
            refreshing = false
        }
    }

    // Set up pull refresh state
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(
                currentRoute = currentRoute(navController = navController),
                onNavigateToHome = {
                    // Navigasi ke home dengan menghapus backstack
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
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = AppTheme.colors.surface,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Item",
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
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 30.dp)
            ) {
                WelcomeTopBar(navController = navController)
                Spacer(modifier = Modifier.height(14.dp))

                SearchBarMenu(
                    query = query,
                    onQueryChange = { newQuery ->
                        // Tambahkan log untuk debugging
                        Log.d("SearchDebug", "Query Changed: $newQuery")
                        // Update query state
                        query = newQuery

                        // Cek apakah string pencarian benar-benar kosong
                        if (newQuery.isEmpty()) {
                            // Jika query kosong, gunakan filter kategori dan subkategori yang aktif
                            val selectedCategoryIds = viewModel.selectedCategoryIds.value
                            val selectedSubCategoryIds = viewModel.selectedSubCategoryIds.value

                            if (selectedCategoryIds.isEmpty() && selectedSubCategoryIds.isEmpty()) {
                                // Jika tidak ada filter yang aktif, fetch semua item menu
                                viewModel.fetchMenuItems()
                            } else {
                                // Jika ada filter aktif, terapkan filter tanpa query pencarian
                                viewModel.filterMenuItems(selectedCategoryIds, selectedSubCategoryIds)
                            }
                        } else {
                            // Jika query tidak kosong, lakukan pencarian dengan mempertimbangkan filter aktif
                            viewModel.searchMenuItems(newQuery)
                        }
                    },
                    placeholder = "Cari Menu",
                    onFilterClick = { showFilter = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.onSurface),
                        contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(menuItems) { menu ->
                            MenuCard(
                                menu = menu,
                                categories = categoryMap,
                                subCategories = subCategoryMap,
                                onDeleteClick = { id ->
                                    selectedMenuId = id
                                    showDeleteDialog = true },
                                onEditClick = { menuId ->
                                    navController.navigate("edit_menu/$menuId")
                                }
                            )
                        }
                    }

                    // Pull refresh indicator
                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = AppTheme.colors.surface,
                        contentColor = Color.White
                    )
                }
            }
            Box(modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd)) {
                FilterSidebarMenu(
                    isVisible = showFilter,
                    onDismiss = { showFilter = false },
                    modifier = Modifier.align(Alignment.TopEnd),
                    categories = viewModel.categories.collectAsState().value.map {
                        FilterChipItem(it.id, it.name)
                    },
                    subCategories = viewModel.subCategories.collectAsState().value.map {
                        FilterChipItem(it.id, it.name)
                    },
                    onResetFilter = {
                        // Reset filter dengan mengirim list kosong
                        viewModel.filterMenuItems(emptyList(), emptyList())
                    },
                    onApplyFilter = { categoryIds, subCategoryIds ->
                        // Terapkan filter kategori dan subkategori
                        viewModel.filterMenuItems(categoryIds, subCategoryIds)
                    }
                )
            }
        }
    }
}