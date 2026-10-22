package dev.codcow.kasirku.features.menuManager.ui.screens
import dev.codcow.kasirku.ui.components.CategoryChips
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
import androidx.activity.compose.BackHandler
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
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.MenuCard
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManager(
    navController: NavController,
    viewModel: MenuManagerViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {
        navController.navigate(Screen.CreateMenu.route) // Tambahkan ini
    },
    onEditClick: (Int) -> Unit = {}
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    val selectedSubCategoryIds by viewModel.selectedSubCategoryIds.collectAsState()
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

    var selectedMenuId by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus menu?",
            message = "Menu \u201C${menuItems.find { it.id == selectedMenuId }?.name.orEmpty()}\u201D akan dihapus dan tidak bisa dikembalikan. Riwayat transaksi yang sudah ada tidak berubah.",
            confirmLabel = "Hapus menu",
            destructive = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                        selectedMenuId?.let { id ->
                            viewModel.deleteMenuItem(id)
                        }
                        showDeleteDialog = false
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
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AddFab("Tambah menu") { onAddClick() }
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
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                GreenBand(
                    title = "Menu",
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.fullBleed(20.dp).background(AppTheme.colors.hero.first()).statusBarsPadding()
                ) {
                    SearchPill(query = query,
                    onQueryChange = { newQuery ->
                        query = newQuery
                        viewModel.searchMenuItems(newQuery)
                    },
                    placeholder = "Cari menu"
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                CategoryChips(
                    categories = categories,
                    subCategories = subCategories,
                    selectedCategoryId = selectedCategoryIds.firstOrNull(),
                    selectedSubCategoryId = selectedSubCategoryIds.firstOrNull(),
                    onSelect = { categoryId, subCategoryId ->
                        viewModel.filterMenuItems(listOfNotNull(categoryId), listOfNotNull(subCategoryId))
                    }
                )


                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp),
                                            ) {
                        item { ListHint("Ketuk menu untuk mengubah nama, harga, atau fotonya") }
itemsIndexed(menuItems, key = { _, it -> it.id }) { i, menu ->
                            MenuCard(
                                        index = i,
                                        count = menuItems.size,
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
