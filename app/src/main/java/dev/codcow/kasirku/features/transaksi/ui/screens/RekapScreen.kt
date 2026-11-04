package dev.codcow.kasirku.features.transaksi.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.components.BandPill
import dev.codcow.kasirku.ui.components.DuoIcon
import dev.codcow.kasirku.ui.components.GreenBand

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import dev.codcow.kasirku.ui.util.rememberReduceMotion
import CustomBottomNavigation
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.features.beranda.ui.components.SearchBarMenu
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import dev.codcow.kasirku.R
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionPemasukan
import dev.codcow.kasirku.core.data.model.pengeluaran.TransactionPengeluaran
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.compose
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import androidx.compose.ui.text.style.TextOverflow
import dev.codcow.kasirku.ui.util.formatRupiahMines

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun RekapScreen(
    navController: NavController,
    viewModel: TransaksiViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorPemasukan by viewModel.errorPemasukan.collectAsState()
    val errorPengeluaran by viewModel.errorPengeluaran.collectAsState()
    val allPemasukan by viewModel.allPemasukan.collectAsState()
    val allPengeluaran by viewModel.allPengeluaran.collectAsState()

    val pemasukanPeriod by viewModel.pemasukanPeriod.collectAsState()
    val pengeluaranPeriod by viewModel.pengeluaranPeriod.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchQueryPeng by remember { mutableStateOf("") }

    var isAmountVisible by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val selectedTabIndex = pagerState.currentPage

    val pdf by viewModel.pdf.collectAsState()
    val isPdfLoading = pdf == TransaksiViewModel.PdfState.Loading

    var refreshing by remember { mutableStateOf(false) }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            if (selectedTabIndex == 0) viewModel.fetchAllPemasukanAcrossPages(pemasukanPeriod.start, pemasukanPeriod.end)
            else viewModel.fetchAllPengeluaranAcrossPages(pengeluaranPeriod.start, pengeluaranPeriod.end)
            delay(800)
            refreshing = false
        }
    }


    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isLoading) { // Observe perubahan pada isLoading
        refreshing = isLoading // Set refreshing true saat isLoading true
        if (!isLoading) {
            delay(800) // Optional delay untuk visual pull-to-refresh
            refreshing = false
        }
    }

    // Load a tab the first time it comes into view on this visit (today); the other one waits until
    // swiped to. targetPage, not currentPage, so loading starts as the swipe begins.
    val loadedTabs = remember { mutableSetOf<Int>() }
    LaunchedEffect(pagerState.targetPage) {
        if (loadedTabs.add(pagerState.targetPage)) {
            if (pagerState.targetPage == 0) viewModel.fetchAllPemasukanAcrossPages("", "")
            else viewModel.fetchAllPengeluaranAcrossPages("", "")
        }
    }

    val totalPemasukan = remember(allPemasukan) {
        allPemasukan.sumOf {
            it.total_amount
                .replace("Rp ", "", ignoreCase = true)
                .toDoubleOrNull() ?: 0.0
        }
    }


    val totalPengeluaran = remember(allPengeluaran) {
        allPengeluaran.sumOf {
            it.total_amount
                .replace("Rp ", "", ignoreCase = true)
                .toDoubleOrNull() ?: 0.0
        }
    }



    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time) }
    val period = if (selectedTabIndex == 0) pemasukanPeriod else pengeluaranPeriod
    val start = period.start.ifEmpty { today }
    val end = period.end.ifEmpty { today }
    var showDates by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    PdfStateEffect(pdf, snackbarHostState, viewModel::resetPdfState)

    // Same date filter the side panel had: applies to the open tab, keeps the search
    fun applyDates(startDate: String, endDate: String) {
        if (selectedTabIndex == 0) {
            if (searchQuery.isEmpty()) viewModel.fetchAllPemasukanAcrossPages(startDate, endDate)
            else viewModel.searchRekapPemasukan(searchQuery, startDate, endDate)
        } else {
            if (searchQueryPeng.isEmpty()) viewModel.fetchAllPengeluaranAcrossPages(startDate, endDate)
            else viewModel.searchRekapPengeluaran(searchQueryPeng, startDate, endDate)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { CustomBottomNavigation(navController = navController) },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.CreatePengeluaran.route) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Catat pengeluaran", style = AppTheme.typography.paragraph1Bold) },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GreenBand(
                title = "Rekap",
                modifier = Modifier.background(AppTheme.colors.hero.first()).statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 72.dp),
                actions = {
                    BandPill(onClick = { showDates = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(rangeLabel(start, end, today), style = AppTheme.typography.paragraph2Semibold)
                    }
                    BandPill(onClick = {
                        val type = if (selectedTabIndex == 0) "pemasukan" else "pengeluaran"
                        viewModel.downloadRekapPdf(start, end, type, "rekap_${type}_${LocalDate.now()}.pdf")
                    }, enabled = !isPdfLoading) {
                        if (isPdfLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AppTheme.colors.onHero)
                        else Icon(Icons.Default.Download, contentDescription = "Unduh PDF", modifier = Modifier.size(18.dp))
                        Text("PDF", style = AppTheme.typography.paragraph2Semibold)
                    }
                }
            ) {
                // Two tabs as a segmented pill on the band
                Row(
                    Modifier.fillMaxWidth().background(AppTheme.colors.onHero.copy(alpha = 0.16f), CircleShape).padding(4.dp).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Pemasukan", "Pengeluaran").forEachIndexed { index, label ->
                        val on = selectedTabIndex == index
                        Box(
                            Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp)
                                .clip(CircleShape)
                                .background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .selectable(selected = on, role = Role.Tab) { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = AppTheme.typography.paragraph2Semibold,
                                color = if (on) AppTheme.colors.deep else AppTheme.colors.onHero
                            )
                        }
                    }
                }
            }
            val breakdown = remember(allPemasukan) {
                allPemasukan.groupBy { it.payment_method.lowercase() }
                    .mapValues { (_, v) -> v.sumOf { it.total_amount.toDoubleOrNull() ?: 0.0 } }
            }
            HorizontalPager(
                state = pagerState,
                // Card overlaps the band, like Beranda
                modifier = Modifier.fillMaxWidth().weight(1f).offset(y = (-56).dp)
            ) { page ->
                val pemasukan = page == 0
                RekapPage(
                    totalLabel = if (pemasukan) "Total pemasukan" else "Total pengeluaran",
                    total = if (pemasukan) totalPemasukan else totalPengeluaran,
                    isAmountVisible = isAmountVisible,
                    onToggleAmountVisibility = { isAmountVisible = !isAmountVisible },
                    breakdown = if (pemasukan) breakdown else null,
                    query = if (pemasukan) searchQuery else searchQueryPeng,
                    onQueryChange = { newQuery ->
                        if (pemasukan) {
                            searchQuery = newQuery
                            if (newQuery.isEmpty()) viewModel.fetchAllPemasukanAcrossPages(start, end) else viewModel.searchRekapPemasukan(newQuery, start, end)
                        } else {
                            searchQueryPeng = newQuery
                            if (newQuery.isEmpty()) viewModel.fetchAllPengeluaranAcrossPages(pengeluaranPeriod.start, pengeluaranPeriod.end)
                            else viewModel.searchRekapPengeluaran(newQuery, pengeluaranPeriod.start, pengeluaranPeriod.end)
                        }
                    },
                    historyTitle = if (pemasukan) "Riwayat pemasukan" else "Riwayat pengeluaran",
                    isLoading = isLoading,
                    error = if (pemasukan) errorPemasukan else errorPengeluaran,
                    emptyText = if (pemasukan) "Tidak ada data pemasukan" else "Tidak ada data pengeluaran",
                    refreshing = refreshing,
                    onRefresh = ::refresh,
                    rows = if (pemasukan) allPemasukan.map { t ->
                        RekapRow(t.nama_transaksi, "${formatJamSaja(t.created_at)}, ${t.payment_method.lowercase()}", formatRupiah(t.total_amount).replace("Rp ", "Rp"), t.payment_method)
                    } else allPengeluaran.map { t ->
                        RekapRow(t.nama_transaksi, formatTanggalSaja(t.created_at), formatRupiahMines(t.total_amount).replace("-Rp ", "−Rp"), null)
                    }
                )
            }
        }
    }

    if (showDates) {
        DateRangeSheet(
            initialStart = start,
            initialEnd = end,
            onDismiss = { showDates = false },
            onToday = { showDates = false; applyDates(today, today) },
            onApply = { s, e -> showDates = false; applyDates(s, e) }
        )
    }
}

private class RekapRow(val name: String?, val meta: String, val amount: String, val method: String?)

private fun rangeLabel(start: String, end: String, today: String): String {
    if (start == today && end == today) return "Hari ini"
    val fmt = DateTimeFormatter.ofPattern("d MMM", Locale("id", "ID"))
    fun d(v: String) = runCatching { LocalDate.parse(v).format(fmt) }.getOrDefault(v)
    return if (start == end) d(start) else "${d(start)} – ${d(end)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RekapPage(
    totalLabel: String,
    total: Double,
    isAmountVisible: Boolean,
    onToggleAmountVisibility: () -> Unit,
    breakdown: Map<String, Double>?,
    query: String,
    onQueryChange: (String) -> Unit,
    historyTitle: String,
    isLoading: Boolean,
    error: String?,
    emptyText: String,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    rows: List<RekapRow>,
) {
    val pullRefreshState = rememberPullToRefreshState()
    fun rp(v: Double) = if (isAmountVisible) formatRupiah(v.toString()).replace("Rp ", "Rp") else "Rp●●●●"
    Box(Modifier.fillMaxSize().pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = onRefresh)) {
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 136.dp)) {
            item {
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
                            Text(totalLabel, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(rp(total), style = AppTheme.typography.paragraph1Bold.copy(fontSize = 28.sp, letterSpacing = (-0.5).sp))
                        }
                        IconButton(
                            onClick = onToggleAmountVisibility,
                            modifier = Modifier.size(44.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        ) {
                            Icon(
                                if (isAmountVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (isAmountVisible) "Sembunyikan jumlah" else "Tampilkan jumlah"
                            )
                        }
                    }
                    breakdown?.let { byMethod ->
                        val sum = byMethod.values.sum().takeIf { it > 0 } ?: 1.0
                        listOf(Triple("tunai", "Tunai", AppIcons.cash), Triple("digital", "Digital", AppIcons.qr), Triple("deposit", "Deposit", AppIcons.card)).forEach { (key, label, icon) ->
                            val v = byMethod[key] ?: 0.0
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                    DuoIcon(icon, 22.dp)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row {
                                        Text(label, style = AppTheme.typography.paragraph2Semibold, modifier = Modifier.weight(1f))
                                        Text(rp(v), style = AppTheme.typography.paragraph2Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (v / sum).toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                                        strokeCap = StrokeCap.Round,
                                        drawStopIndicator = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Cari transaksi", style = AppTheme.typography.paragraph2) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = CircleShape,
                    textStyle = AppTheme.typography.paragraph2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(historyTitle, style = AppTheme.typography.paragraph1Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
            }
            when {
                isLoading && rows.isEmpty() -> {}
                error != null || rows.isEmpty() -> item {
                    Text(
                        text = error ?: emptyText,
                        style = AppTheme.typography.paragraph1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> itemsIndexed(rows) { i, r ->
                    Row(Modifier.heightIn(min = 64.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        r.method?.let { m ->
                            Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                DuoIcon(when (m.lowercase()) { "tunai", "cash" -> AppIcons.cash; "deposit" -> AppIcons.card; else -> AppIcons.qr }, 22.dp)
                            }
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(r.name.takeIf { !it.isNullOrEmpty() } ?: "Tanpa nama", style = AppTheme.typography.paragraph1Semibold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(r.meta, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(if (isAmountVisible) r.amount else "Rp●●●", style = AppTheme.typography.paragraph1Bold)
                    }
                    if (i < rows.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        PullToRefreshDefaults.Indicator(
            isRefreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = MaterialTheme.colorScheme.primary,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/** Tanggal mulai / Tanggal selesai, the same range filter the side panel had. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSheet(
    initialStart: String,
    initialEnd: String,
    onDismiss: () -> Unit,
    onToday: () -> Unit,
    onApply: (String, String) -> Unit
) {
    val context = LocalContext.current
    var start by remember { mutableStateOf(LocalDate.parse(initialStart)) }
    var end by remember { mutableStateOf(LocalDate.parse(initialEnd)) }
    val fmt = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")) }
    fun pick(current: LocalDate, onPicked: (LocalDate) -> Unit) {
        android.app.DatePickerDialog(context, { _, y, m, d -> onPicked(LocalDate.of(y, m + 1, d)) }, current.year, current.monthValue - 1, current.dayOfMonth).show()
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Pilih tanggal", style = AppTheme.typography.heading5Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Tanggal mulai" to start, "Tanggal selesai" to end).forEachIndexed { i, (label, date) ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(label, style = AppTheme.typography.paragraph2Semibold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(
                            onClick = { pick(date) { if (i == 0) start = it else end = it } },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(date.format(fmt), style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            if (end.isBefore(start)) {
                Text("Tanggal selesai harus sama atau setelah tanggal mulai.", style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.error)
            } else {
                Text("Berlaku untuk tab yang sedang dibuka, juga untuk Unduh PDF.", style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onToday, shape = CircleShape, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    Text("Hari ini lagi", style = AppTheme.typography.paragraph1Bold)
                }
                Button(
                    onClick = { onApply(start.toString(), end.toString()) },
                    enabled = !end.isBefore(start),
                    shape = CircleShape,
                    modifier = Modifier.weight(1.4f).heightIn(min = 52.dp)
                ) { Text("Pakai", style = AppTheme.typography.paragraph1Bold) }
            }
        }
    }
}

private fun getCurrentDateTime(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
    return current.format(formatter)
}

fun formatJamSaja(timestamp: String): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        offsetDateTime.format(formatter)
    } catch (e: Exception) {
        "Format Tidak Valid"
    }
}

fun formatTanggalSaja(timestamp: String): String {
    return try {
        val offsetDateTime = OffsetDateTime.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault())
        offsetDateTime.format(formatter)
    } catch (e: Exception) {
        "Format Tidak Valid"
    }
}
