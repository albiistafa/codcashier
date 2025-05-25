package dev.codcow.kasirku.features.subKategoriManager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.codcow.kasirku.core.data.model.subkategori.CreateSubkategoriRequest
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.launch
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubKategori(
    navController: NavController,
    modifier: Modifier = Modifier,
    subKategoriId: Int,
    viewModel: SubKategoriViewModel = hiltViewModel(),
    kategoriViewModel: KategoriManagerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = { navController.navigateUp() }
) {
    val subKategori by viewModel.selectedSubKategori.collectAsState()
    val categories by kategoriViewModel.kategoriItems.collectAsState()

    var namaKategori by remember(subKategori) { mutableStateOf(subKategori?.name ?: "") }
    var selectedCategoryId by remember(subKategori) { mutableStateOf<Int?>(subKategori?.category_id) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    val scope = rememberCoroutineScope()

    // Fetch the subKategori data when the composable is first launched
    LaunchedEffect(subKategoriId) {
        viewModel.getSubKategoriById(subKategoriId)
    }

    // Fetch kategori data
    LaunchedEffect(Unit) {
        kategoriViewModel.fetchKategoriItems()
    }

    // Navigate back when successful
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Sub-Kategori",
                        style = AppTheme.typography.labelBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AppTheme.colors.onSurface)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Field Nama Kategori dengan tanda bintang merah
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nama Sub-Kategori",
                    style = AppTheme.typography.paragraph2,
                    color = Color.Black
                )
                Text(
                    text = "*",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            OutlinedTextField(
                value = namaKategori,
                onValueChange = { namaKategori = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AppTheme.colors.surface,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kategori Dropdown
            Text(
                text = "Kategori *",
                style = AppTheme.typography.paragraph2,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategoryId?.let { categoryId ->
                        categories.find { it.id == categoryId }?.name ?: ""
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Pilih Kategori") }
                )

                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier
                        .background(Color.White)
                        .exposedDropdownSize()
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = category.name,
                                    style = AppTheme.typography.paragraph2
                                )
                            },
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == category.id) {
                                    null
                                } else {
                                    category.id
                                }
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Simpan di bagian bawah
            Button(
                onClick = {
                    if (namaKategori.isNotBlank() && selectedCategoryId != null && subKategori != null) {
                        scope.launch {
                            val updatedSubKategori = subKategori!!.copy(
                                name = namaKategori.trim(),
                                category_id = selectedCategoryId!!
                            )
                            viewModel.updateSubKategori(subKategoriId, updatedSubKategori)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.surface,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isLoading && namaKategori.isNotBlank() && selectedCategoryId != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Simpan",
                        style = AppTheme.typography.paragraph1,
                        color = Color.White
                    )
                }
            }

            // Error Message
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = Color.Red,
                    style = AppTheme.typography.paragraph2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}