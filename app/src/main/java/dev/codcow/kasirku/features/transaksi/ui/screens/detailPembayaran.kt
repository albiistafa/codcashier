package dev.codcow.kasirku.features.transaksi.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.core.data.model.transaksi.Data
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.components.DuoIcon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import captureComposableToBitmap
import dev.codcow.kasirku.features.pdf.ReceiptContent
import dev.codcow.kasirku.features.pdf.createReceiptBitmap
import dev.codcow.kasirku.features.pdf.saveBitmapAsPdf
import dev.codcow.kasirku.features.warung.ui.screens.warungViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPembayaran(
    navController: NavController,
    transactionId: Int,
    received: Long? = null,
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    warungViewModel: warungViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val warungName by warungViewModel.warung.collectAsState()
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val transaction by transaksiViewModel.currentTransaction.collectAsState()
    val context = LocalContext.current

    // This screen is the stack root after checkout; Back always lands on the menu
    fun backToMenu() {
        navController.navigate(Screen.Menu.route) { popUpTo(0) { inclusive = true } }
    }
    BackHandler { backToMenu() }

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("DetailPembayaran", "Screen initialized with transactionId: $transactionId")
    }

    LaunchedEffect(warungName) {
        Log.d("DetailPembayaran", "Warung name: ${warungName?.name ?: "null"}")
    }

    LaunchedEffect(transactionId) {
        Log.d("DetailPembayaran", "Fetching transaction with ID: $transactionId")
        transaksiViewModel.getTransactionById(transactionId)
    }

    LaunchedEffect(transaction) {
        Log.d("DetailPembayaran", "Transaction updated: ${transaction != null}")
        transaction?.let {
            Log.d("DetailPembayaran", "Transaction details: id=${it.id}, total=${it.total_amount}, method=${it.payment_method}")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun downloadReceipt() {
        val current = transaction ?: run {
            scope.launch { snackbarHostState.showSnackbar("Data transaksi belum tersedia") }
            return
        }
        val saved = runCatching {
            saveBitmapAsPdf(context, createReceiptBitmap(context, current, warungName))
        }.onFailure { Log.e("PDF_DEBUG", "Receipt PDF failed", it) }.getOrNull()
        scope.launch {
            if (saved != null) {
                snackbarHostState.showSnackbar("Struk berhasil diunduh")
            } else if (snackbarHostState.showSnackbar(
                    "Gagal mengunduh struk", actionLabel = "Coba lagi", duration = SnackbarDuration.Long
                ) == SnackbarResult.ActionPerformed
            ) {
                downloadReceipt()
            }
        }
    }

    LaunchedEffect(isLoading) {
        Log.d("DetailPembayaran", "Loading state changed: $isLoading")
    }

    // Prevent auto-navigation when loading
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Show error state
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (error?.contains("404") == true) "Transaksi tidak ditemukan."
                        else "Struk belum bisa dimuat. Transaksi tetap tersimpan dan bisa dilihat di menu Transaksi.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = AppTheme.typography.paragraph1
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = ::backToMenu
                ) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    val current = transaction
    val total = current?.total_amount?.toDoubleOrNull()?.toLong()
    // Change first: it's what the cashier hands back next
    val change = if (received != null && total != null) received - total else null
    val hero = AppTheme.colors.hero.first()
    val onHero = AppTheme.colors.onHero
    fun rp(v: Long?) = formatRupiah((v ?: 0L).toDouble().toString()).replace("Rp ", "Rp")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
          // Band and struk scroll together: an offset struk inside its own scroll area got clipped under the band
          Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(0f to hero, 0.7f to hero.copy(alpha = 0.92f), 1f to MaterialTheme.colorScheme.surfaceVariant))
                    .statusBarsPadding()
                    .padding(top = 36.dp, bottom = 72.dp)
                    .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    Modifier.size(60.dp).background(AppTheme.colors.money, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = AppTheme.colors.onMoney, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(4.dp))
                val showChange = change != null && change > 0
                Text(if (showChange) "Kembalian" else "Lunas", style = AppTheme.typography.paragraph1, color = onHero.copy(alpha = 0.85f))
                Text(
                    rp(if (showChange) change else total),
                    style = AppTheme.typography.heading1Bold.copy(letterSpacing = (-1).sp),
                    color = onHero
                )
                Text(
                    if (received != null) "dari ${rp(received)} tunai" else methodLabel(current?.payment_method),
                    style = AppTheme.typography.paragraph2,
                    color = onHero.copy(alpha = 0.8f)
                )
            }

            current?.let { trx ->
                Receipt(trx, warungName?.name, modifier = Modifier.offset(y = (-48).dp).padding(horizontal = 20.dp))
            }
          }

            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = ::backToMenu,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.money, contentColor = AppTheme.colors.onMoney),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                ) {
                    Text("Pesanan baru", style = AppTheme.typography.paragraph1Bold)
                }
                TextButton(
                    onClick = ::downloadReceipt,
                    enabled = current != null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Unduh struk", style = AppTheme.typography.paragraph1Bold)
                }
            }
        }
    }
}

private fun methodLabel(method: String?) = when (method?.lowercase()) {
    "tunai", "cash" -> "Tunai"
    "deposit" -> "Deposit"
    null, "" -> ""
    else -> "Digital"
}

/** Struk: torn bottom edge, dashed rules, real lines from the saved transaction. */
@Composable
private fun Receipt(trx: Data, warungName: String?, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val edge = with(LocalDensity.current) { receiptBottomShape(16.dp.toPx(), 7.dp.toPx()) }
    val icon = when (methodLabel(trx.payment_method)) {
        "Tunai" -> AppIcons.cash
        "Deposit" -> AppIcons.card
        else -> AppIcons.qr
    }
    Column(
        modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = AppTheme.colors.hero.first(), spotColor = AppTheme.colors.hero.first())
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(edge)
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { DuoIcon(icon, 22.dp) }
            Column(Modifier.weight(1f)) {
                Text(trx.nama_transaksi, style = AppTheme.typography.paragraph1Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(warungName, "${formatTanggalSaja(trx.created_at)}, ${formatJamSaja(trx.created_at)}").joinToString(", "),
                    style = AppTheme.typography.paragraph2, color = muted, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            val pending = trx.status == "pending"
            Text(
                if (pending) "Belum dibayar" else "Lunas",
                style = AppTheme.typography.paragraph2Semibold,
                color = if (pending) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        }
        DashedRule()
        trx.transaction_details.forEach { d ->
            Row {
                Text("${d.quantity}× ${d.menu_name}", style = AppTheme.typography.paragraph2, modifier = Modifier.weight(1f))
                Text(formatRupiah(d.subtotal).replace("Rp ", "Rp"), style = AppTheme.typography.paragraph2)
            }
        }
        DashedRule()
        Row {
            Text("Total", style = AppTheme.typography.paragraph1Bold, modifier = Modifier.weight(1f))
            Text(formatRupiah(trx.total_amount).replace("Rp ", "Rp"), style = AppTheme.typography.paragraph1Bold)
        }
    }
}

@Composable
private fun DashedRule() {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(color, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())))
    }
}

/** Torn bottom edge for the struk card. */
private fun receiptBottomShape(toothWidthPx: Float, toothHeightPx: Float) = GenericShape { size, _ ->
    val teeth = (size.width / toothWidthPx).toInt().coerceAtLeast(1)
    val w = size.width / teeth
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - toothHeightPx)
    repeat(teeth) { i ->
        val x = size.width - i * w
        lineTo(x - w / 2, size.height)
        lineTo(x - w, size.height - toothHeightPx)
    }
    close()
}
