package dev.codcow.kasirku.kategoriManager.ui.screens
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.components.readableWidth
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel
import dev.codcow.kasirku.ui.components.ListHint
import dev.codcow.kasirku.ui.components.AddFab
import androidx.compose.foundation.lazy.itemsIndexed
import dev.codcow.kasirku.ui.components.ConfirmDialog
import dev.codcow.kasirku.ui.components.ErrorState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.currentRoute
import dev.codcow.kasirku.features.subKategoriManager.ui.screens.SubKategoriViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.components.SubKategoriListItem
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubKategoriManager(
    navController: NavController,
    viewModel: SubKategoriViewModel = hiltViewModel(),
    kategoriViewModel: KategoriManagerViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {
        navController.navigate(Screen.CreateSubKategori.route)
    },
    onEditClick: (Int) -> Unit = {}
) {
    val subkategoriItems by viewModel.subkategoriItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var query by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var selectedSubId by remember { mutableStateOf<Int?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus subkategori?",
            message = "Subkategori \u201C${subkategoriItems.find { it.id == selectedSubId }?.name.orEmpty()}\u201D akan dihapus dan tidak bisa dikembalikan.",
            confirmLabel = "Hapus subkategori",
            destructive = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                        selectedSubId?.let { id ->
                            viewModel.deleteSubKategoriItem(id)
                        }
                        showDeleteDialog = false
            }
        )
    }

    val kategoriItems by kategoriViewModel.kategoriItems.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.fetchSubKategoriItems()
        kategoriViewModel.fetchKategoriItems()
    }

    LaunchedEffect(isLoading) { // Observe perubahan pada isLoading
        refreshing = isLoading // Set refreshing true saat isLoading true
        if (!isLoading) {
            delay(800) // Optional delay untuk visual pull-to-refresh
            refreshing = false
        }
    }

    fun refresh() {
        coroutineScope.launch {
            refreshing = true
            // Refresh data from API or ViewModel
            viewModel.fetchSubKategoriItems() // Assuming there's a fetch function in ViewModel
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
            AddFab("Tambah subkategori") { onAddClick() }
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
                    title = "Subkategori",
                    onBack = { navController.popBackStack() },
                    modifier = Modifier.fullBleed(20.dp).background(AppTheme.colors.hero.first()).statusBarsPadding()
                ) {
                    SearchPill(query = query,
                    onQueryChange = { newQuery ->
                        Log.d("SearchDebug", "Query Changed: $newQuery")
                        // Cek apakah string pencarian benar-benar kosong
                        if (newQuery.isEmpty()) {
                            query = newQuery
                            viewModel.fetchSubKategoriItems() // Panggil fetchMenuItems() hanya jika newQuery benar-benar kosong
                        } else {
                            query = newQuery
                            viewModel.searchSubKategoriItems(newQuery)
                        }
                    },
                    placeholder = "Cari subkategori"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(isRefreshing = refreshing, state = pullRefreshState, onRefresh = ::refresh)
                ) {
                    LazyColumn(
                        modifier = Modifier.readableWidth()
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp),
                                            ) {
                        // Display loading indicator if data is loading
                        if (isLoading) {
                            item {

                            }
                        }
                        // Display error message if there is an error
                        else if (error != null) {
                            item {
                                ErrorState(message = error ?: "", onRetry = { viewModel.fetchSubKategoriItems() }, icon = AppIcons.subkategori)
                            }
                        }
                        // Display subkategori items
                        else {
                            item { ListHint("Ketuk subkategori untuk mengganti namanya") }
// Grouped under their kategori, in the kategori list's order
                            val names = kategoriItems.associate { it.id to it.name }
                            subkategoriItems.groupBy { it.category_id }
                                .toList()
                                .sortedBy { (catId, _) -> kategoriItems.indexOfFirst { it.id == catId }.let { if (it < 0) Int.MAX_VALUE else it } }
                                .forEach { (catId, group) ->
                                    item(key = "cat-$catId") {
                                        Text(
                                            names[catId] ?: "Tanpa kategori",
                                            style = AppTheme.typography.paragraph2Semibold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                        )
                                    }
                                    itemsIndexed(group, key = { _, it -> it.id }) { i, subkategori ->
                                        SubKategoriListItem(
                                            index = i,
                                            count = group.size,
                                            subkategori = subkategori,
                                            onEditClick = { subKategoriId -> navController.navigate("edit_subKategori/$subKategoriId") },
                                            onDeleteClick = { id ->
                                                selectedSubId = id
                                                showDeleteDialog = true
                                            }
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
        }
    }
}