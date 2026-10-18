package dev.codcow.kasirku.features.transaksi.ui.screens

import androidx.compose.foundation.border
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.features.deposit.ui.screens.DepositViewModel
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuManagerViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val backToMenu = { if (!navController.popBackStack()) navController.navigate(Screen.Menu.route) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Pesanan",
                        style = AppTheme.typography.labelBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        CheckoutPane(
            onCartEmpty = { backToMenu() },
            onAddMore = { backToMenu() },
            onTransactionDone = navController::navigateAfterCheckout,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/** Clears the whole stack after checkout, so Back can't return to a paid cart. */
fun NavController.navigateAfterCheckout(transactionId: Int, pending: Boolean, received: Long? = null) {
    navigate(if (pending) Screen.Menu.route else Screen.DetailPembayaran.createRoute(transactionId, received)) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Cart list + one Bayar button, without its own navigation, so it can be a full screen (phone)
 * or sit beside the menu grid (expanded width). Paying happens in [PaymentSheet].
 * Pass the caller's [menuViewModel] to share the cart. A null [onAddMore] hides "Tambah Pesanan".
 */
@Composable
fun CheckoutPane(
    onCartEmpty: () -> Unit,
    onAddMore: (() -> Unit)?,
    onTransactionDone: (transactionId: Int, pending: Boolean, received: Long?) -> Unit,
    modifier: Modifier = Modifier,
    menuViewModel: MenuManagerViewModel = hiltViewModel(),
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
) {
    val isSuccess by transaksiViewModel.isSuccess.collectAsState()
    val cart by menuViewModel.cart.collectAsState()
    val scope = rememberCoroutineScope()
    var showPay by remember { mutableStateOf(false) }
    val hasItems = cart.items.any { it.quantity > 0 }

    // Emptied by hand (not by a finished payment): leave the cart screen
    LaunchedEffect(hasItems) {
        if (!hasItems && !isSuccess) {
            delay(300)
            onCartEmpty()
        }
    }

    if (showPay) {
        PaymentSheet(
            menuViewModel = menuViewModel,
            transaksiViewModel = transaksiViewModel,
            onDismiss = { showPay = false },
            onDone = { id, pending, received ->
                showPay = false
                onTransactionDone(id, pending, received)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            items(items = cart.items, key = { it.menuId }) { item ->
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    CartItemSwipeToDelete(
                        item = item,
                        onIncrement = {
                            scope.launch { menuViewModel.updateCartItemQuantity(item.menuId, item.quantity + 1) }
                        },
                        onDecrement = {
                            scope.launch {
                                if (item.quantity > 1) menuViewModel.updateCartItemQuantity(item.menuId, item.quantity - 1)
                                else menuViewModel.removeFromCart(item.menuId)
                            }
                        },
                        onRemove = { scope.launch { menuViewModel.removeFromCart(item.menuId) } }
                    )
                }
            }

            if (!hasItems) item {
                Text(
                    "Belum ada pesanan. Ketuk menu untuk menambahkan.",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (onAddMore != null) item {
                TextButton(
                    onClick = onAddMore,
                    modifier = Modifier.padding(horizontal = 8.dp).heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Outlined.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah menu lain", style = AppTheme.typography.paragraph1Medium)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "Total · ${cart.items.sumOf { it.quantity }} item",
                    style = AppTheme.typography.paragraph1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(formatRupiah(cart.totalPrice.toString()), style = AppTheme.typography.heading4Bold)
            }
            Button(
                onClick = { showPay = true },
                enabled = hasItems,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Text("Bayar", style = AppTheme.typography.paragraph1Semibold)
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp), // thumbnail 10dp + 8dp padding
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            dev.codcow.kasirku.ui.components.MenuPhoto(
                photo = item.photo,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.paragraph1Bold
                )
                Text(
                    text = formatRupiah(item.price.toDouble().toString()),
                    style = AppTheme.typography.paragraph2
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = "Kurangi",
                    tint = MaterialTheme.colorScheme.primary)
            }
            // fixed width: Poppins digits are proportional, so 9 → 10 would nudge the + button
            Text(
                item.quantity.toString(),
                style = AppTheme.typography.paragraph2,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 28.dp)
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.AddCircleOutline, contentDescription = "Tambah",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CartItemSwipeToDelete(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onRemove()
            value == SwipeToDismissBoxValue.EndToStart
        },
        positionalThreshold = { distance -> distance * 0.5f }
    )

    // Reset state jika item berubah
    LaunchedEffect(item.menuId) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) dismissState.reset()
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val swiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val color by animateColorAsState(
                targetValue = if (swiping) MaterialTheme.colorScheme.error else Color.Transparent,
                label = "Dismiss Background Color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(color, shape = RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.alpha(if (swiping) 1f else 0f)
                )
            }
        }
    ) {
        CartItemRow(
            item = item,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onRemove = onRemove
        )
    }
}
