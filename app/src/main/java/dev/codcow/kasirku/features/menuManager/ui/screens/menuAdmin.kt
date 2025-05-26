package dev.codcow.kasirku.features.menuManager.ui.screens

import CustomBottomNavigation
import LoadingMenuCard
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel
import dev.codcow.kasirku.features.menuManager.component.AnimatedCartIndicator
import dev.codcow.kasirku.ui.components.FilterSidebarMenu
import dev.codcow.kasirku.ui.components.MenuCardPemesanan

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MenuAdmin(
    navController: NavController,
    viewModel: MenuManagerViewModel = hiltViewModel(),
    kategoriManagerViewModel: KategoriManagerViewModel = hiltViewModel()
) {
    var showFilter by remember { mutableStateOf(false) }
    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val cart by viewModel.cart.collectAsState()
    var showCartIndicator by remember { mutableStateOf(false) }

    var query by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val itemQuantities = remember { mutableStateMapOf<Int, Int>() }

    val categoryMap = remember(categories) {
        categories.associate { it.id to it.name }
    }

    val subCategoryMap = remember(subCategories) {
        subCategories.associate { it.id to it.name }
    }



    LaunchedEffect(cart.items) {
        showCartIndicator = cart.items.any { it.quantity > 0 }
    }


    LaunchedEffect(cart) {
        // Reset all quantities to 0
        itemQuantities.clear()

        // Set quantities based on what's actually in the cart
        cart.items.forEach { cartItem ->
            itemQuantities[cartItem.menuId] = cartItem.quantity
        }
    }

    LaunchedEffect(cart.items.size) {
        showCartIndicator = cart.items.any { it.quantity > 0 }
        Log.d("MenuPegawai", "Cart Size Changed: ${cart.items.size}, showCartIndicator: $showCartIndicator")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMenuItems()
        viewModel.fetchCategories()
        viewModel.fetchSubCategories()
    }
    // Function to handle refresh action
    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            // Refresh data from API
            viewModel.fetchCategories()
            viewModel.fetchMenuItems()
            // Add small delay to show refresh indicator
            delay(800)
            refreshing = false
        }
    }

    // Set up pull refresh state
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Box(modifier = Modifier.fillMaxSize()) {


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
                        // Tampilkan pesan error
                        Text(text = "Error: $error", color = Color.Red)
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
                                val quantity = itemQuantities.getOrPut(menu.id) { 0 }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()

                                ) {
                                    MenuCardPemesanan(
                                        menu = menu,
                                        quantity = quantity,
                                        onIncrement = { selectedMenu ->
                                            itemQuantities[selectedMenu.id] = quantity + 1
                                            viewModel.addToCart(
                                                selectedMenu,
                                                1
                                            ) // Tambah satu item ke cart
                                        },
                                        onDecrement = { selectedMenu ->
                                            if (quantity > 0) {
                                                itemQuantities[selectedMenu.id] = quantity - 1
                                                viewModel.removeFromCart(selectedMenu.id) // Kurangi satu item dari cart
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
                onClick = { navController.navigate("detail") },
                isVisible = showCartIndicator,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp)
            )
            }
        }

        Box(modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd)) {
            val categories by viewModel.categories.collectAsState()
            val subCategories by viewModel.subCategories.collectAsState()

            // Debug: Log untuk melihat data categories dan subCategories
            LaunchedEffect(categories, subCategories) {
                println("MenuPegawaiDebug: Categories and SubCategories Updated")
                println("MenuPegawaiDebug: Categories count: ${categories.size}")
                println("MenuPegawaiDebug: Categories: ${categories.map { "${it.id}-${it.name}" }}")
                println("MenuPegawaiDebug: SubCategories count: ${subCategories.size}")
                println("MenuPegawaiDebug: SubCategories: ${subCategories.map { "${it.id}-${it.name}-category_id:${it.category_id}" }}")
            }

            // Pastikan mapping subkategori ke kategori benar
            val subCategoryToCategoryMap = remember(subCategories) {
                val mapping = subCategories.associate { subCategory ->
                    subCategory.id to subCategory.category_id
                }
                println("MenuPegawaiDebug: Created subCategoryToCategoryMap")
                println("MenuPegawaiDebug: Mapping size: ${mapping.size}")
                println("MenuPegawaiDebug: Mapping content: $mapping")
                mapping
            }

            FilterSidebarMenu(
                isVisible = showFilter,
                onDismiss = { showFilter = false },
                modifier = Modifier.align(Alignment.TopEnd),
                categories = categories.map { FilterChipItem(it.id, it.name) },
                subCategories = subCategories.map { FilterChipItem(it.id, it.name) },
                subCategoryToCategoryMap = subCategoryToCategoryMap,
                onResetFilter = {
                    println("MenuPegawaiDebug: Reset filter called")
                    viewModel.filterMenuItems(emptyList(), emptyList())
                },
                onApplyFilter = { categoryIds, subCategoryIds ->
                    println("MenuPegawaiDebug: Apply filter called")
                    println("MenuPegawaiDebug: Selected categoryIds: $categoryIds")
                    println("MenuPegawaiDebug: Selected subCategoryIds: $subCategoryIds")
                    viewModel.filterMenuItems(categoryIds, subCategoryIds)
                }
            )
        }
    }
}
