package dev.codcow.kasirku.features.transaksi.ui.screens

import CustomBottomNavigation
import android.content.Intent
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.core.content.FileProvider
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
import dev.codcow.kasirku.ui.components.RecapFilterSidebar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.compose
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Calendar
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.text.style.TextOverflow
import dev.codcow.kasirku.ui.util.formatRupiahMines

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
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
    val isFilterVisible by viewModel.isFilterVisible.collectAsState()

    val currentRekapPemasukanStartDate by viewModel.currentRekapPemasukanStartDate.collectAsState()
    val currentRekapPemasukanEndDate by viewModel.currentRekapPemasukanEndDate.collectAsState()
    val currentRekapPengeluaranStartDate by viewModel.currentRekapPengeluaranStartDate.collectAsState()
    val currentRekapPengeluaranEndDate by viewModel.currentRekapPengeluaranEndDate.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchQueryPeng by remember { mutableStateOf("") }

    var isAmountVisible by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val selectedTabIndex = pagerState.currentPage

    val isPdfLoading by viewModel.isPdfLoading.collectAsState()
    val isPdfSuccess by viewModel.isPdfSuccess.collectAsState()
    val pdfError by viewModel.pdfError.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()

    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            viewModel.fetchAllPemasukanAcrossPages(currentRekapPemasukanStartDate, currentRekapPemasukanEndDate)
            viewModel.fetchAllPengeluaranAcrossPages(currentRekapPengeluaranStartDate, currentRekapPengeluaranEndDate)
            delay(800)
            refreshing = false
        }
    }

    BackHandler(enabled = isFilterVisible) {
        viewModel.closeFilterSidebar()
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    LaunchedEffect(isLoading) { // Observe perubahan pada isLoading
        refreshing = isLoading // Set refreshing true saat isLoading true
        if (!isLoading) {
            delay(800) // Optional delay untuk visual pull-to-refresh
            refreshing = false
        }
    }

    LaunchedEffect(pdfFile) {
        pdfFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)

                // Reset PDF state after viewing
                viewModel.resetPdfState()
            } catch (e: Exception) {
                ("Failed to open PDF: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllPemasukanAcrossPages("", "")
        viewModel.fetchAllPengeluaranAcrossPages("", "")
    }

    val totalPemasukan = remember(allPemasukan) {
        allPemasukan.sumOf {
            it.total_amount
                .replace("Rp ", "", ignoreCase = true)
                .toDoubleOrNull() ?: 0.0
        }
    }

    val totalPemasukanFormatted = remember(totalPemasukan) {
        val formattedAmount = NumberFormat.getNumberInstance(Locale("id", "ID"))
            .format(totalPemasukan)
        "Rp $formattedAmount"
    }

    val totalPengeluaran = remember(allPengeluaran) {
        allPengeluaran.sumOf {
            it.total_amount
                .replace("Rp ", "", ignoreCase = true)
                .toDoubleOrNull() ?: 0.0
        }
    }

    val totalPengeluaranFormatted = remember(totalPengeluaran) {
        val formattedAmount = NumberFormat.getNumberInstance(Locale("id", "ID"))
            .format(totalPengeluaran)
        "-Rp $formattedAmount"
    }

    // Simplified display logic to ensure search results are shown properly
    val displayPemasukan = allPemasukan

    val displayPengeluaran = allPengeluaran

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
                // Filter chips can go here if needed
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .weight(1f)
                    .background(Color(0xFFFFFFFF)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Rekap",
                    style = AppTheme.typography.labelBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = AppTheme.colors.surface,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF6D8E22) // Warna indikator yang Anda inginkan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        content = {
                            val contentColor = if (selectedTabIndex == 0) AppTheme.colors.surface else Color.Gray
                            CompositionLocalProvider(LocalContentColor provides contentColor) {
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.stash_article_duotone__1_),
                                        contentDescription = "",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Pemasukan",
                                        style = AppTheme.typography.paragraph2
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        content = {
                            val contentColor = if (selectedTabIndex == 1) AppTheme.colors.surface else Color.Gray
                            CompositionLocalProvider(LocalContentColor provides contentColor) {
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.hugeicons_chart_down),
                                        contentDescription = "",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Pengeluaran",
                                        style = AppTheme.typography.paragraph2
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }

                // Horizontal pager for tab content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { page ->
                    when (page) {
                        0 -> RekapTransaksiContent(
                            totalPemasukanFormatted = totalPemasukanFormatted,
                            isAmountVisible = isAmountVisible,
                            onToggleAmountVisibility = { isAmountVisible = !isAmountVisible },
                            query = searchQuery,
                            onQueryChange = { newQuery ->
                                searchQuery = newQuery
                                viewModel.updateSearchQuery(newQuery) // Use consistent function name
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
                                val startDateToSend = if (currentRekapPemasukanStartDate.isNotEmpty()) currentRekapPemasukanStartDate else today
                                val endDateToSend = if (currentRekapPemasukanEndDate.isNotEmpty()) currentRekapPemasukanEndDate else today
                                Log.d("onQueryChange", "today: $today")
                                Log.d("onQueryChange", "startDateToSend: $startDateToSend")
                                Log.d("onQueryChange", "endDateToSend: $endDateToSend")
                                if (newQuery.isEmpty()) {
                                    viewModel.fetchAllPemasukanAcrossPages(startDateToSend, endDateToSend)
                                } else {
                                    viewModel.searchRekapPemasukan(newQuery, startDateToSend, endDateToSend)
                                }
                            },
                            onFilterClick = { viewModel.toggleFilterSidebar() },
                            isLoading = isLoading,
                            error = errorPemasukan,
                            displayPemasukan = displayPemasukan
                        )
                        1 -> PengeluaranContent(
                            totalPengeluaranFormatted = totalPengeluaranFormatted,
                            isAmountVisible = isAmountVisible,
                            onToggleAmountVisibility = { isAmountVisible = !isAmountVisible },
                            query = searchQueryPeng,
                            onQueryChange = { newQuery ->
                                searchQueryPeng = newQuery
                                viewModel.updateSearchQuery(newQuery)
                                if (newQuery.isEmpty()) {
                                    viewModel.fetchAllPengeluaranAcrossPages(currentRekapPengeluaranStartDate, currentRekapPengeluaranEndDate)
                                } else {
                                    viewModel.searchRekapPengeluaran(newQuery, currentRekapPengeluaranStartDate, currentRekapPengeluaranEndDate)
                                }
                            },
                            onFilterClick = { viewModel.toggleFilterSidebar() },
                            isLoading = isLoading,
                            error = errorPengeluaran,
                            displayPengeluaran = displayPengeluaran
                        )
                    }
                }
            }

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
                        popUpTo(Screen.Transaksi.route) { inclusive = true }
                    }
                }
            )
        }


        Box(modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd)) {
            RecapFilterSidebar(
                isVisible = isFilterVisible,
                onDismiss = { viewModel.closeFilterSidebar() },
                modifier = Modifier.align(Alignment.TopEnd),
                onApplyDateFilter = { startDateInput, endDateInput ->
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

                    val startDate = if (startDateInput.isNotEmpty()) startDateInput else today
                    val endDate = if (endDateInput.isNotEmpty()) endDateInput else today

                    if (selectedTabIndex == 0) {
                        if (searchQuery.isEmpty()) {
                            viewModel.fetchAllPemasukanAcrossPages(startDate, endDate)
                        } else {
                            viewModel.applyRekapPemasukanFilters(startDate, endDate)
                            viewModel.searchRekapPemasukan(searchQuery, startDate, endDate)
                        }
                    } else {
                        if (searchQueryPeng.isEmpty()) {
                            viewModel.fetchAllPengeluaranAcrossPages(startDate, endDate)
                        } else {
                            viewModel.applyRekapPengeluaranFilters(startDate, endDate)
                            viewModel.searchRekapPengeluaran(searchQueryPeng, startDate, endDate)
                        }
                    }
                },
                onResetFilter = {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

                    if (selectedTabIndex == 0) { // Set filter ke hari ini
                        viewModel.fetchAllPemasukanAcrossPages(today, today) // Fetch data hari ini
                    } else { // Set filter ke hari ini
                        viewModel.fetchAllPengeluaranAcrossPages(today, today) // Fetch data hari ini
                    }
                },
                isPdfLoading = isPdfLoading,
                isPdfSuccess = isPdfSuccess,
                pdfError = pdfError,
                onDownloadPdf = { startDate, endDate ->
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
                    val startDate = if (startDate.isNotEmpty()) startDate else today
                    val endDate = if (endDate.isNotEmpty()) endDate else today
                    val type = if (selectedTabIndex == 0) "pemasukan" else "pengeluaran"
                    val fileName = "rekap_${type}_${LocalDate.now()}.pdf"
                    viewModel.downloadRekapPdf(startDate, endDate, type, fileName)
                },
                onResetPdfState = {
                    viewModel.resetPdfState()
                },
            )
        }

        if (isFilterVisible && selectedTabIndex == 1) {

        } else if (!isFilterVisible && selectedTabIndex == 1) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.CreatePengeluaran.route)
                },
                modifier = Modifier
                    .padding(bottom = 120.dp, end = 16.dp)
                    .align(Alignment.BottomEnd), // Tetap di pojok kanan bawah saat filter tidak terlihat
                containerColor = AppTheme.colors.surface,
                shape = CircleShape,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Pengeluaran"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RekapTransaksiContent(
    totalPemasukanFormatted: String,
    isAmountVisible: Boolean,
    onToggleAmountVisibility: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    isLoading: Boolean,
    error: String?,
    displayPemasukan: List<TransactionPemasukan>,
    viewModel : TransaksiViewModel = hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Gradient Animation")
    val errorPemasukan by viewModel.errorPemasukan.collectAsState()

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // besar nilai ini mempengaruhi kecepatan
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing), // 10 detik
            repeatMode = RepeatMode.Reverse
        ),
        label = "Offset Animation"
    )

    val animatedBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF97AD56), Color(0xFF6D8E22), Color(0xFF97AD56)),
        start = Offset(0f, 0f),
        end = Offset(animatedOffset, animatedOffset)
    )

    var searchText by remember { mutableStateOf(query) }

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            delay(800)
            refreshing = false
        }
    }
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)


    LaunchedEffect(isLoading) {
        refreshing = isLoading
        if (!isLoading) {
            delay(800)
            refreshing = false
        }
    }

    // Sync with external query changes
    LaunchedEffect(query) {
        if (query != searchText) {
            searchText = query
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(20.dp), // Membuat card lebih melengkung
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent // karena kita pakai background
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(animatedBrush, shape = RoundedCornerShape(20.dp)) // Membuat background juga melengkung
            ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total Pemasukan",
                            style = AppTheme.typography.paragraph2,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAmountVisible) totalPemasukanFormatted else "Rp ●●●●●●●",
                                style = AppTheme.typography.heading4Bold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = onToggleAmountVisibility,
                                modifier = Modifier
                                    .size(40.dp) // ukuran lingkaran
                                    .background(
                                        color = Color.White.copy(alpha = 0.2f), // latar bulat transparan
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isAmountVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle amount visibility",
                                    tint = Color.White
                                )
                            }
                        }
                        Text(
                            text = "Terakhir diperbarui: ${getCurrentDateTime()}",
                            style = AppTheme.typography.paragraph2,
                            color = Color.Black
                        )
                    }
            }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                ,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Hitung total untuk setiap kategori pembayaran
            val tunaiTotal = displayPemasukan
                .filter { it.payment_method.equals("Tunai", ignoreCase = true) }
                .sumOf { it.total_amount.replace("Rp ", "", ignoreCase = true).toDoubleOrNull() ?: 0.0 }

            val digitalTotal = displayPemasukan
                .filter { it.payment_method.equals("Digital", ignoreCase = true) }
                .sumOf { it.total_amount.replace("Rp ", "", ignoreCase = true).toDoubleOrNull() ?: 0.0 }

            val depositTotal = displayPemasukan
                .filter { it.payment_method.equals("Deposit", ignoreCase = true) }
                .sumOf { it.total_amount.replace("Rp ", "", ignoreCase = true).toDoubleOrNull() ?: 0.0 }

            // Card untuk Tunai
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tunai",
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isAmountVisible) "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(tunaiTotal)}" else "Rp ●●●",
                        style = AppTheme.typography.paragraph2Bold,
                        color = AppTheme.colors.surface
                    )
                }
            }

            // Card untuk Digital
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Digital",
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isAmountVisible) "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(digitalTotal)}" else "Rp ●●●",
                        style = AppTheme.typography.paragraph2Bold,
                        color = AppTheme.colors.surface
                    )
                }
            }

            // Card untuk Deposit
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Deposit",
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isAmountVisible) "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(depositTotal)}" else "Rp ●●●",
                        style = AppTheme.typography.paragraph2Bold,
                        color = AppTheme.colors.surface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        SearchBarMenu(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Cari Transaksi",
            onFilterClick = onFilterClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Riwayat Transaksi Header
        Text(
            text = "Riwayat Transaksi",
            style = AppTheme.typography.paragraph1Medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ){
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .pullRefresh(pullRefreshState),
                    contentAlignment = Alignment.Center
                ) {

                }
            } else if (errorPemasukan != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                        .pullRefresh(pullRefreshState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$errorPemasukan",
                        style = AppTheme.typography.paragraph1,
                        color = Color.Gray
                    )
                }
            } else if (displayPemasukan.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data pemasukan",
                        style = AppTheme.typography.paragraph1,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(displayPemasukan) { transaction ->
                        TransaksiItem(transaction)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = AppTheme.colors.surface,
                contentColor = Color.White
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PengeluaranContent(
    totalPengeluaranFormatted: String,
    isAmountVisible: Boolean,
    onToggleAmountVisibility: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    isLoading: Boolean,
    error: String?,
    displayPengeluaran: List<TransactionPengeluaran>,
    viewModel: TransaksiViewModel = hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Gradient Animation")
    val errorPengeluaran by viewModel.errorPengeluaran.collectAsState()

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // besar nilai ini mempengaruhi kecepatan
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing), // 10 detik
            repeatMode = RepeatMode.Reverse
        ),
        label = "Offset Animation"
    )

    val animatedBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF97AD56), Color(0xFF6D8E22), Color(0xFF97AD56)),
        start = Offset(0f, 0f),
        end = Offset(animatedOffset, animatedOffset)
    )

    var searchText by remember { mutableStateOf(query) }

    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            delay(800)
            refreshing = false
        }
    }
    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    LaunchedEffect(isLoading) {
        refreshing = isLoading
        if (!isLoading) {
            delay(800)
            refreshing = false
        }
    }

    // Sync with external query changes
    LaunchedEffect(query) {
        if (query != searchText) {
            searchText = query
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(20.dp), // Membuat card lebih melengkung
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent // karena kita pakai background
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        animatedBrush,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Total Pengeluaran",
                        style = AppTheme.typography.paragraph2,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAmountVisible) totalPengeluaranFormatted else "Rp ●●●●●●●",
                            style = AppTheme.typography.heading4Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onToggleAmountVisibility,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f), // latar bulat transparan
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isAmountVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle amount visibility",
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Terakhir diperbarui: ${getCurrentDateTime()}",
                        style = AppTheme.typography.paragraph2,
                        color = Color.Black
                    )
                }
            }
        }

        // Search Bar
        SearchBarMenu(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Cari Pengeluaran",
            onFilterClick = onFilterClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Riwayat Pengeluaran Header
        Text(
            text = "Riwayat Pengeluaran",
            style = AppTheme.typography.paragraph1Medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ){
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .pullRefresh(pullRefreshState),
                    contentAlignment = Alignment.Center
                ) {

                }
            } else if (errorPengeluaran != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                        .pullRefresh(pullRefreshState),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$errorPengeluaran",
                        style = AppTheme.typography.paragraph1,
                        color = Color.Gray
                    )
                }
            } else if (displayPengeluaran.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data pengeluaran",
                        style = AppTheme.typography.paragraph1,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(displayPengeluaran) { pengeluaran ->
                        PengeluaranItem(pengeluaran)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = AppTheme.colors.surface,
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun TransaksiItem(transaction: TransactionPemasukan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        Color(0x33ADADAD),
                        shape = RoundedCornerShape(percent = 50),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.maki_arrow),
                    contentDescription = "",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val transactionName = transaction.nama_transaksi.takeIf { !it.isNullOrEmpty() }
                        ?: "No Transaction Name"
                    val shortenedName = if (transactionName.length > 14) {
                        transactionName.take(10) + "…"
                    } else {
                        transactionName
                    }

                    Text(
                        text = shortenedName,
                        style = AppTheme.typography.paragraph2Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = formatJamSaja(transaction.created_at),
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatRupiah(transaction.total_amount),
                        style = AppTheme.typography.paragraph1Bold,
                        color = AppTheme.colors.surface
                    )
                    Text(
                        text = "#${transaction.payment_method}",
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PengeluaranItem(transaction: TransactionPengeluaran) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        Color(0x33ADADAD),
                        shape = RoundedCornerShape(percent = 50),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.maki_arrowpengeluaran),
                    contentDescription = "",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val transactionName = transaction.nama_transaksi.takeIf { !it.isNullOrEmpty() }
                        ?: "No Transaction Name"
                    val shortenedName = if (transactionName.length > 14) {
                        transactionName.take(10) + "…"
                    } else {
                        transactionName
                    }
                    Text(
                        text = shortenedName,
                        style = AppTheme.typography.paragraph2Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = formatTanggalSaja(transaction.created_at),
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatRupiahMines(transaction.total_amount),
                        style = AppTheme.typography.paragraph1Bold,
                        color = AppTheme.colors.onHighlightSurface
                    )
                    Text(
                        text = "#${transaction.payment_method}",
                        style = AppTheme.typography.paragraph2,
                        color = Color.Gray
                    )
                }
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
