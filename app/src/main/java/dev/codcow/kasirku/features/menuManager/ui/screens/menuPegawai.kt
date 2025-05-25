package dev.codcow.kasirku.features.menuManager.ui.screens

import CustomBottomNavigation
import LoadingMenuCard
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
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
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.*
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.features.menuManager.component.AnimatedCartIndicator
import dev.codcow.kasirku.ui.components.FilterSidebarMenu
import dev.codcow.kasirku.ui.components.MenuCardPemesanan

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MenuPegawai(
    navController: NavController,
    viewModel: MenuManagerViewModel = hiltViewModel(),
) {
    var showFilter by remember { mutableStateOf(false) }
    val menuItems by viewModel.menuItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val showCartIndicator by remember {
        derivedStateOf {
            cart.items.any { it.quantity > 0 }
        }
    }

    BackHandler(enabled = showFilter) {
        showFilter = false
    }

    var query by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val itemQuantities = remember { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(Unit) {
        snapshotFlow { navController.currentBackStackEntry?.lifecycle?.currentState }
            .collect { lifecycleState ->
                if (lifecycleState == Lifecycle.State.RESUMED) {
                    viewModel.refreshCart()
                }
            }
    }

    LaunchedEffect(cart) {
        itemQuantities.clear()
        cart.items.forEach { item ->
            if (item.quantity > 0) {
                itemQuantities[item.menuId] = item.quantity
            }
        }
        Log.d("MenuPegawai", "Cart Updated: ${cart.items.size} items with total > 0: ${cart.items.count { it.quantity > 0 }}")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMenuItems()
        viewModel.fetchCategories()
        viewModel.fetchSubCategories()
        viewModel.refreshCart()
    }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            viewModel.fetchCategories()
            viewModel.fetchMenuItems()
            delay(800)
            refreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Box(modifier = Modifier.fillMaxSize()) {

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

                Text("Menu", style = AppTheme.typography.labelBold)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    if (isLoading) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 20.dp, bottom = 50.dp)
                        ) {
                            items(8) {
                                LoadingMenuCard()
                            }
                        }
                    } else if (error != null) {

                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .pullRefresh(pullRefreshState),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 20.dp, bottom = 50.dp)
                        ) {
                            items(menuItems) { menu ->
                                val quantity by produceState(initialValue = viewModel.getItemQuantity(menu.id)) {
                                    viewModel.cart.collect { cart ->
                                        value = cart.items.find { it.menuId == menu.id }?.quantity ?: 0
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()

                                ) {
                                    MenuCardPemesanan(
                                        menu = menu,
                                        quantity = quantity,
                                        onIncrement = { selectedMenu ->
                                            viewModel.addToCart(selectedMenu, 1)
                                        },
                                        onDecrement = { selectedMenu ->
                                            if (quantity > 0) {
                                                if (quantity > 1) {
                                                    viewModel.updateCartItemQuantity(selectedMenu.id, quantity - 1)
                                                } else {
                                                    viewModel.removeFromCart(selectedMenu.id)
                                                }
                                            }
                                        },
                                    )
                                }
                            }
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
            AnimatedCartIndicator(
                cartItems = cart.items.filter { it.quantity > 0 },
                totalPrice = cart.totalPrice,
                onClick = {
                    navController.navigate(Screen.CartDetail.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                },
                isVisible = showCartIndicator,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp)
            )
            }
        }

        Box(modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd)) {
            val subCategoryToCategoryMap = viewModel.subCategories.collectAsState().value.associate {
                it.id to it.category_id
            }
            FilterSidebarMenu(
                isVisible = showFilter,
                onDismiss = { showFilter = false },
                modifier = Modifier.align(Alignment.TopEnd),
                categories = viewModel.categories.collectAsState().value.map {
                    FilterChipItem(it.id, it.name) },
                subCategories = viewModel.subCategories.collectAsState().value.map {
                    FilterChipItem(it.id, it.name) },
                subCategoryToCategoryMap = subCategoryToCategoryMap,
                onResetFilter = {
                    viewModel.filterMenuItems(emptyList(), emptyList()) },
                onApplyFilter = { categoryIds, subCategoryIds ->
                    viewModel.filterMenuItems(categoryIds, subCategoryIds)
                }
            )
        }
    }
}
