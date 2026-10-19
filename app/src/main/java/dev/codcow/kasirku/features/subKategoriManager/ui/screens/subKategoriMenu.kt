package dev.codcow.kasirku.kategoriManager.ui.screens
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
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubKategoriManager(
    navController: NavController,
    viewModel: SubKategoriViewModel = hiltViewModel(),
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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Konfirmasi Hapus", style = AppTheme.typography.heading5Bold,
                color = MaterialTheme.colorScheme.primary) },
            text = { Text("Apakah Anda yakin ingin menghapus sub-kategori ini?", style = AppTheme.typography.paragraph2) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedSubId?.let { id ->
                            viewModel.deleteSubKategoriItem(id)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Ya", style = AppTheme.typography.paragraph2Bold,
                        color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Tidak", style = AppTheme.typography.paragraph2Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSubKategoriItems()
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
        bottomBar = {
            CustomBottomNavigation(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Sub-Kategori",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(bottom = 80.dp,top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display loading indicator if data is loading
                        if (isLoading) {
                            item {

                            }
                        }
                        // Display error message if there is an error
                        else if (error != null) {
                            item {
                                Text(
                                    text = "Error: $error",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        // Display subkategori items
                        else {
                            items(subkategoriItems, key = { it.id }) { subkategori ->
                                SubKategoriListItem(
                                    subkategori = subkategori,
                                    onEditClick = { subKategoriId ->
                                        navController.navigate("edit_subKategori/$subKategoriId")
                                    },
                                    onDeleteClick = { id ->
                                        selectedSubId = id
                                        showDeleteDialog = true }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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