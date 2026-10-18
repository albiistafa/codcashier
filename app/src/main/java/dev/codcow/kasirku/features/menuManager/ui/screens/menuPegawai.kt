package dev.codcow.kasirku.features.menuManager.ui.screens

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import CustomBottomNavigation
import isExpandedWindow
import isWideWindow
import navigateToTab
import LoadingMenuCard
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.Lifecycle
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.features.menuManager.component.AnimatedCartIndicator
import dev.codcow.kasirku.features.transaksi.ui.screens.CheckoutPane
import dev.codcow.kasirku.features.transaksi.ui.screens.navigateAfterCheckout
import dev.codcow.kasirku.ui.components.MenuCardPemesanan
import dev.codcow.kasirku.ui.components.MenuLoadState
import dev.codcow.kasirku.ui.components.CategoryChips
import dev.codcow.kasirku.features.transaksi.ui.screens.PaymentSheet
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPegawai(
    navController: NavController,
    viewModel: MenuManagerViewModel = hiltViewModel(),
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
) {
    var showPay by remember { mutableStateOf(false) }
    val menuItems by viewModel.menuItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsState()
    val selectedSubCategoryIds by viewModel.selectedSubCategoryIds.collectAsState()
    val pendingCount by transaksiViewModel.pendingCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val showCartIndicator by remember {
        derivedStateOf {
            cart.items.any { it.quantity > 0 }
        }
    }


    var query by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val itemQuantities = remember(cart.items) {
        cart.items.associate { it.menuId to it.quantity }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { navController.currentBackStackEntry?.lifecycle?.currentState }
            .collect { lifecycleState ->
                if (lifecycleState == Lifecycle.State.RESUMED) {
                    viewModel.refreshCart()
                    transaksiViewModel.refreshPendingCount()
                }
            }
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

    val pullRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize()) {

    Scaffold(
        bottomBar = {
            CustomBottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        val expanded = isExpandedWindow()
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Header scrolls with the grid: in landscape a fixed header left no room for the menu
            val header: @Composable () -> Unit = {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    WelcomeTopBar(navController = navController)
                    Spacer(modifier = Modifier.height(14.dp))

                    SearchBarMenu(
                        query = query,
                        onQueryChange = { newQuery ->
                            query = newQuery
                            viewModel.searchMenuItems(newQuery)
                        },
                        placeholder = "Cari Menu"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CategoryChips(
                        categories = categories,
                        subCategories = subCategories,
                        selectedCategoryId = selectedCategoryIds.firstOrNull(),
                        selectedSubCategoryId = selectedSubCategoryIds.firstOrNull(),
                        onSelect = { categoryId, subCategoryId ->
                            viewModel.filterMenuItems(listOfNotNull(categoryId), listOfNotNull(subCategoryId))
                        }
                    )
                    if (pendingCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        UnpaidOrdersButton(
                            count = pendingCount,
                            onClick = { navController.navigateToTab(Screen.Transaksi.route) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
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
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) { header() }
                            items(8) {
                                LoadingMenuCard()
                            }
                        }
                    } else if (error != null || menuItems.isEmpty()) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            header()
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
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item(key = "header", span = { GridItemSpan(maxLineSpan) }) { header() }
                            items(menuItems, key = { it.id }) { menu ->
                                val quantity = itemQuantities[menu.id] ?: 0
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
            if (!expanded) AnimatedCartIndicator(
                cartItems = cart.items.filter { it.quantity > 0 },
                totalPrice = cart.totalPrice,
                // Keep Menu under the cart so Back returns here instead of closing the app
                onOpen = { navController.navigate(Screen.CartDetail.route) { launchSingleTop = true } },
                onPay = { showPay = true },
                isVisible = showCartIndicator,
                modifier = Modifier.align(if (isWideWindow()) Alignment.BottomEnd else Alignment.BottomCenter)
            )
            }
            // Expanded: the cart is always beside the grid instead of a separate screen
            if (expanded) {
                VerticalDivider()
                Column(modifier = Modifier.width(400.dp).fillMaxHeight()) {
                    Text(
                        "Detail Pesanan",
                        style = AppTheme.typography.labelBold,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                    )
                    CheckoutPane(
                        menuViewModel = viewModel,
                        onCartEmpty = {},
                        onAddMore = null,
                        onTransactionDone = navController::navigateAfterCheckout,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        }

        if (showPay) {
            PaymentSheet(
                menuViewModel = viewModel,
                transaksiViewModel = transaksiViewModel,
                onDismiss = { showPay = false },
                onDone = { id, pending, received ->
                    showPay = false
                    navController.navigateAfterCheckout(id, pending, received)
                }
            )
        }
    }
}

/** Unpaid orders live on the cashier screen, not only in the Transaksi tab. */
@Composable
private fun UnpaidOrdersButton(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
            Text("$count pesanan belum dibayar", style = AppTheme.typography.paragraph2Medium, modifier = Modifier.weight(1f))
            Text("Lihat", style = AppTheme.typography.paragraph2Semibold)
        }
    }
}
