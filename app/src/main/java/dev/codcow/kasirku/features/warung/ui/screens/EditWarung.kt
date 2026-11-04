package dev.codcow.kasirku.features.warung.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import dev.codcow.kasirku.ui.components.AppIcons
import dev.codcow.kasirku.ui.components.ConfirmDialog
import dev.codcow.kasirku.ui.components.RowIcon
import java.io.File
import dev.codcow.kasirku.ui.components.BandTopBar

import android.net.Uri
import android.util.Log
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
import androidx.navigation.NavController
import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.core.data.model.warung.RequestWarung
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.codcow.kasirku.core.utils.Result
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWarung(
    modifier: Modifier = Modifier,
    viewModel: warungViewModel = hiltViewModel(),
    qrisViewModel: QrisViewModel = hiltViewModel(),
    navController: NavController
) {
    val qrisPath by qrisViewModel.path.collectAsState()
    val qrisSaving by qrisViewModel.saving.collectAsState()
    val qrisError by qrisViewModel.error.collectAsState()
    var confirmRemoveQris by remember { mutableStateOf(false) }
    // System photo picker: no storage permission needed
    val pickQris = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(qrisViewModel::save)
    }
    val cachedName by viewModel.cachedWarungName.collectAsState()
    val warungName by viewModel.warung.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val _isSuccess by viewModel.isSuccess.collectAsState()

    val scope = rememberCoroutineScope()

    var warung by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchWarungIfChanged()
    }

    LaunchedEffect(warungName) {
        warungName?.let {
            warung = it.name
        }
    }
    // The field opened empty when only the cached name was known (Beranda shows that same cache)
    LaunchedEffect(cachedName) {
        if (warung.isBlank()) cachedName?.let { warung = it }
    }

    LaunchedEffect(_isSuccess) {
        if (_isSuccess) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            BandTopBar(title = "Warung", onBack = { navController.navigateUp() })
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
                    text = "Nama Warung",
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
                value = warung,
                onValueChange = { warung = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                supportingText = { Text("Tampil di Beranda dan di struk", style = AppTheme.typography.paragraph2) },
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
                    if (warung.isNotBlank()) {
                        scope.launch {
                            val request = RequestWarung(
                                name = warung.trim()
                            )
                            viewModel.PostWarung(request)
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
                enabled = !isLoading && warung.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Simpan nama",
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

            QrisCard(
                path = qrisPath,
                saving = qrisSaving,
                error = qrisError,
                onPick = { pickQris.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onRemove = { confirmRemoveQris = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (confirmRemoveQris) {
        ConfirmDialog(
            title = "Hapus QRIS?",
            message = "Gambar QRIS dihapus dari HP ini. Pembayaran Digital tidak menampilkan QRIS sampai kamu mengunggahnya lagi.",
            confirmLabel = "Hapus QRIS",
            onDismiss = { confirmRemoveQris = false },
            onConfirm = { confirmRemoveQris = false; qrisViewModel.clear() }
        )
    }
}

/** QRIS shown at Bayar Digital. Saved the moment it is picked; no separate Simpan. */
@Composable
private fun QrisCard(path: String?, saving: Boolean, error: String?, onPick: () -> Unit, onRemove: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RowIcon(AppIcons.qr)
            Column(Modifier.weight(1f)) {
                Text("QRIS warung", style = AppTheme.typography.paragraph1Bold)
                Text("Ditampilkan saat pelanggan bayar Digital", style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (path != null) {
            AsyncImage(
                model = File(path),
                contentDescription = "QRIS warung",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPick, enabled = !saving, shape = CircleShape, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text("Ganti QRIS", style = AppTheme.typography.paragraph2Semibold)
                }
                TextButton(onClick = onRemove, enabled = !saving, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Hapus", style = AppTheme.typography.paragraph2Semibold, color = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            OutlinedButton(
                onClick = onPick,
                enabled = !saving,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Menyimpan QRIS…", style = AppTheme.typography.paragraph2Semibold)
                } else {
                    Text("Unggah gambar QRIS dari galeri", style = AppTheme.typography.paragraph2Semibold)
                }
            }
        }
        if (error != null) {
            Text(error, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.error)
        }
        Text(
            "Tersimpan di HP ini saja. Sinkron ke HP lain sedang dikembangkan.",
            style = AppTheme.typography.paragraph2,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}