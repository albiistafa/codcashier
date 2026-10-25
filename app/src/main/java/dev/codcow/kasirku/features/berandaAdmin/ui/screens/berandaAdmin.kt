package dev.codcow.kasirku.features.berandaAdmin.ui.screens
import androidx.compose.ui.platform.LocalDensity
import dev.codcow.kasirku.ui.components.readableWidth

import CustomBottomNavigation
import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionPemasukan
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.features.auth.ui.screens.LoginViewModel
import dev.codcow.kasirku.features.transaksi.ui.screens.formatJamSaja
import dev.codcow.kasirku.features.warung.ui.screens.warungViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.Duo
import dev.codcow.kasirku.ui.components.DuoIcon
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import navigateToTab
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun beranda(
    onNavigateToPegawai: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToKategori: () -> Unit,
    onNavigateToSubKategori: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToTransaksi: () -> Unit,
    onNavigateToWarung: () -> Unit,
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    warungVm: warungViewModel = hiltViewModel(),
    viewModel: BerandaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val warungName by warungVm.cachedWarungName.collectAsState()
    // Re-runs each time the owner comes back here, so a Lunasi elsewhere shows up
    LaunchedEffect(Unit) {
        warungVm.fetchWarungIfChanged()
        viewModel.load()
    }

    val tiles = listOf(
        Tile("Menu", AppIcons.menu, onNavigateToMenu),
        Tile("Kategori", AppIcons.kategori, onNavigateToKategori),
        Tile("Subkategori", AppIcons.subkategori, onNavigateToSubKategori),
        Tile("Deposit", AppIcons.deposit, onNavigateToDeposit),
        Tile("Pegawai", AppIcons.pegawai, onNavigateToPegawai),
        Tile("Kelola transaksi", AppIcons.transaksi, onNavigateToTransaksi),
        Tile("Pengeluaran", AppIcons.pengeluaran) { navController.navigate(Screen.CreatePengeluaran.route) },
        Tile("Warung & QRIS", AppIcons.warung, onNavigateToWarung)
    )

    Scaffold(
        bottomBar = { CustomBottomNavigation(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
        // The green band draws behind the status bar; it pads itself
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            val hero = AppTheme.colors.hero.first()
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to hero, 0.62f to hero.copy(alpha = 0.92f),
                            1f to MaterialTheme.colorScheme.background
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp)
            ) {
              Column(Modifier.readableWidth()) {
                Header(
                    name = warungName ?: "Warung",
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
                Spacer(Modifier.height(20.dp))
                MoneyCard(
                    state = state,
                    onRetry = viewModel::load,
                    onRekap = { navController.navigateToTab(Screen.Rekap.route) },
                    onSell = { navController.navigateToTab(Screen.Menu.route) }
                )
              }
            }

          Column(Modifier.readableWidth()) {

            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                tiles.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { TileButton(it, Modifier.weight(1f)) }
                    }
                }
            }

            if (state.pending.isNotEmpty()) {
                SectionTitle(
                    title = "Belum dibayar",
                    badge = state.pendingCount,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    RoundIconButton("Semua yang belum dibayar") { navController.navigateToTab(Screen.Transaksi.route) }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    items(state.pending, key = { it.id }) { trx ->
                        PendingCard(trx) { navController.navigate(Screen.PaymentUpdate.createRoute(trx.id)) }
                    }
                }
            }

            SectionTitle(title = "Penjualan terakhir", modifier = Modifier.padding(top = 28.dp)) {
                if (state.salesCount > 0) {
                    TextButton(onClick = { navController.navigateToTab(Screen.Transaksi.route) }) {
                        Text("${state.salesCount} hari ini", style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                when {
                    state.loading && state.recent.isEmpty() ->
                        Text("Memuat…", style = AppTheme.typography.paragraph1, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                    state.recent.isEmpty() ->
                        Text("Belum ada penjualan hari ini. Tekan Mulai jualan untuk mencatat pesanan pertama.", style = AppTheme.typography.paragraph1, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                    else -> state.recent.forEachIndexed { i, trx ->
                        SaleRow(trx, divider = i < state.recent.lastIndex) {
                            navController.navigate(Screen.DetailSelesai.createRoute(trx.id))
                        }
                    }
                }
            }
          }
        }
    }
}

@Composable
private fun Header(name: String, onLogout: () -> Unit) {
    val onHero = AppTheme.colors.onHero
    var menuOpen by remember { mutableStateOf(false) }
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("id", "ID")))
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(AppTheme.colors.money),
            contentAlignment = Alignment.Center
        ) {
            Text(initials(name), color = AppTheme.colors.onMoney, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(
                name, color = onHero, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.paragraph1Bold.copy(fontSize = 18.sp),
                modifier = Modifier.semantics { heading() }
            )
            Text(today, color = onHero.copy(alpha = 0.82f), style = AppTheme.typography.paragraph2)
        }
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(44.dp).clip(CircleShape).background(onHero.copy(alpha = 0.16f))
            ) {
                Icon(Icons.Default.Person, contentDescription = "Akun", tint = onHero)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Keluar") }, onClick = { menuOpen = false; onLogout() })
            }
        }
    }
}

@Composable
private fun MoneyCard(state: BerandaState, onRetry: () -> Unit, onRekap: () -> Unit, onSell: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val showNumbers = !state.loading || state.masuk > 0 || state.keluar > 0
    fun money(v: Double) = if (showNumbers) formatRupiah(v.toString()).replace("Rp ", "Rp") else "Rp…"
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = AppTheme.colors.hero.first(), spotColor = AppTheme.colors.hero.first())
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Sisa uang hari ini", style = AppTheme.typography.paragraph2, color = muted)
                Text(
                    money(state.masuk - state.keluar),
                    style = AppTheme.typography.paragraph1Bold.copy(fontSize = 28.sp, letterSpacing = (-0.5).sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            RoundIconButton("Lihat rekap hari ini", icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, onClick = onRekap)
        }
        if (state.failed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Sebagian data hari ini gagal dimuat.",
                    style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetry) { Text("Coba lagi") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoneyChip("Masuk", money(state.masuk), Modifier.weight(1f))
            MoneyChip("Keluar", money(state.keluar), Modifier.weight(1f))
        }
        Button(
            onClick = onSell,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.money, contentColor = AppTheme.colors.onMoney)
        ) {
            DuoIcon(AppIcons.receipt, 20.dp, fill = Color.Transparent, stroke = AppTheme.colors.onMoney)
            Spacer(Modifier.width(8.dp))
            Text("Mulai jualan", style = AppTheme.typography.paragraph1Bold.copy(fontSize = 16.sp))
        }
    }
}

@Composable
private fun MoneyChip(label: String, value: String, modifier: Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

private class Tile(val label: String, val icon: Duo, val onClick: () -> Unit)

@Composable
private fun TileButton(tile: Tile, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = tile.onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(60.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            DuoIcon(tile.icon, 30.dp)
        }
        // Four tiles share a row, so a long single word ("Subkategori") would break mid-word under a
        // large system font. The label stops growing at 1.05x; the icon carries the meaning.
        // ponytail: fixed cap, switch to 3 columns at large font if more labels get long.
        val fontScale = LocalDensity.current.fontScale
        val k = minOf(fontScale, 1.05f) / fontScale
        Text(
            tile.label, textAlign = TextAlign.Center, maxLines = 2,
            style = AppTheme.typography.paragraph2.copy(fontWeight = FontWeight.SemiBold, fontSize = (12 * k).sp, lineHeight = (16 * k).sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier, badge: Int = 0, trailing: @Composable () -> Unit) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title, style = AppTheme.typography.paragraph1Bold.copy(fontSize = 17.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        if (badge > 0) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.defaultMinSize(22.dp, 22.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun RoundIconButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Filled.ArrowForward, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PendingCard(trx: Transaction, onLunasi: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .width(236.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                trx.nama_transaksi.ifBlank { "Pesanan" }, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(formatJamSaja(trx.created_at), style = AppTheme.typography.paragraph2, color = muted)
        }
        Text(
            itemsSummary(trx.transaction_details.map { it.menu_name to it.quantity }),
            maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTheme.typography.paragraph2, color = muted
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                formatRupiah(trx.total_amount).replace("Rp ", "Rp"),
                style = AppTheme.typography.paragraph1Bold.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
            )
            Button(onClick = onLunasi, shape = CircleShape, contentPadding = PaddingValues(horizontal = 16.dp), modifier = Modifier.height(40.dp)) {
                Text("Lunasi", style = AppTheme.typography.paragraph2.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun SaleRow(trx: TransactionPemasukan, divider: Boolean, onClick: () -> Unit) {
    val method = trx.payment_method.lowercase()
    val icon = when {
        "deposit" in method -> AppIcons.card
        "tunai" in method || "cash" in method -> AppIcons.cash
        else -> AppIcons.qr
    }
    Column(Modifier.clickable(onClick = onClick)) {
        Row(Modifier.heightIn(min = 64.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { DuoIcon(icon, 22.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    // pemasukan details are typed Any by the API model
                    trx.nama_transaksi.ifBlank { itemsSummary(trx.transaction_details.map { "${it.menu_name}" to ((it.quantity as? Number)?.toInt() ?: 1) }) },
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.paragraph1Semibold, color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${formatJamSaja(trx.created_at)}, ${trx.payment_method.lowercase()}",
                    style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatRupiah(trx.total_amount).replace("Rp ", "Rp"),
                style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (divider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

private fun itemsSummary(items: List<Pair<String, Int>>) =
    items.joinToString(", ") { (name, qty) -> if (qty > 1) "$name ×$qty" else name }

private fun initials(name: String) =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "W" }
