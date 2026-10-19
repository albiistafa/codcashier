package dev.codcow.kasirku.features.menuManager.ui.screens
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import dev.codcow.kasirku.ui.components.GreenBand
import dev.codcow.kasirku.ui.components.SearchPill
import dev.codcow.kasirku.ui.components.fullBleed

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import CustomBottomNavigation
import LoadingMenuCard
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import dev.codcow.kasirku.ui.components.MenuLoadState

@OptIn(ExperimentalMaterial3Api::class)
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
    val pullRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize()) {


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CustomBottomNavigation(navController = navController)
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
                    placeholder = "Cari menu",
                    onFilterClick = { showFilter = true }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
                ) {
                    if (isLoading) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 20.dp, bottom = 50.dp)
                        ) {
                            items(8) {
                                LoadingMenuCard()
                            }
                        }
                    } else if (error != null || menuItems.isEmpty()) {
                        MenuLoadState(hasError = error != null, onRetry = {
                            if (query.isNotEmpty()) {
                                viewModel.searchMenuItems(query)
                            } else {
                                viewModel.filterMenuItems(
                                    viewModel.selectedCategoryIds.value,
                                    viewModel.selectedSubCategoryIds.value
                                )
                            }
                            viewModel.fetchCategories()
                            viewModel.fetchSubCategories()
                        })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 20.dp, bottom = 50.dp)
                        ) {
                            items(menuItems, key = { it.id }) { menu ->
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
                                                viewModel.updateCartItemQuantity(selectedMenu.id, quantity - 1)
                                            }
                                        },
                                    )
                                }
                            }
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
            AnimatedCartIndicator(
                cartItems = cart.items.filter { it.quantity > 0 },
                totalPrice = cart.totalPrice,
                // "detail" alone matched no route; the cart screen is CartDetail
                onOpen = { navController.navigate(Screen.CartDetail.route) { launchSingleTop = true } },
                onPay = { navController.navigate(Screen.CartDetail.route) { launchSingleTop = true } },
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
            }

            // Pastikan mapping subkategori ke kategori benar
            val subCategoryToCategoryMap = remember(subCategories) {
                val mapping = subCategories.associate { subCategory ->
                    subCategory.id to subCategory.category_id
                }
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
                    viewModel.filterMenuItems(emptyList(), emptyList())
                },
                onApplyFilter = { categoryIds, subCategoryIds ->
                    viewModel.filterMenuItems(categoryIds, subCategoryIds)
                }
            )
        }
    }
}
