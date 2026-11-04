package dev.codcow.kasirku.features.kategoriManager.ui.screens
import dev.codcow.kasirku.ui.components.BandTopBar

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.codcow.kasirku.core.utils.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateKategori(
    modifier: Modifier = Modifier,
    viewModel: KategoriManagerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var namaKategori by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            BandTopBar(title = "Tambah kategori", onBack = onNavigateBack)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Field Nama Kategori dengan tanda bintang merah
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nama Kategori",
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "*",
                    color = MaterialTheme.colorScheme.error,
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
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Simpan di bagian bawah
            Button(
                onClick = {
                    if (namaKategori.isNotBlank()) {
                        scope.launch {
                            val request = CreateKategoriRequest(
                                name = namaKategori.trim()
                            )
                            viewModel.createKategori(request).collect { result ->
                                when (result) {
                                    is Result.Success -> {
                                        // Refresh data sebelum navigasi
                                        viewModel.fetchKategoriItems()
                                        onNavigateBack()
                                    }
                                    is Result.Error -> {
                                        // Error sudah ditangani di ViewModel
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = !isLoading && namaKategori.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Simpan",
                        style = AppTheme.typography.paragraph1
                    )
                }
            }

            // Error Message
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = AppTheme.typography.paragraph2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}