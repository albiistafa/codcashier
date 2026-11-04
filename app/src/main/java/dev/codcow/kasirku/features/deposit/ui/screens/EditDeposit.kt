package dev.codcow.kasirku.features.deposit.ui.screens
import androidx.compose.foundation.layout.heightIn
import dev.codcow.kasirku.ui.components.ConfirmDialog
import dev.codcow.kasirku.ui.components.BandTopBar

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
import dev.codcow.kasirku.core.data.model.deposit.CreateDataDeposit
import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.codcow.kasirku.core.utils.Result
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeposit(
    modifier: Modifier = Modifier,
    customerId: Int,
    viewModel: DepositViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    val selectedEditDeposit by viewModel.selectedEditDeposit.collectAsState()
    var namaDeposit by remember { mutableStateOf("") }
    var phoneDeposit by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val scope = rememberCoroutineScope()
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        viewModel.getCustomerById(customerId)
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.navigateUp()
        }
    }

    LaunchedEffect(selectedEditDeposit) {
        selectedEditDeposit?.let {
            Log.d("EditDeposit", "Processing deposit data: $it")

            if (it.id != 0 || it.customer_id != 0) {
                namaDeposit = it.name
                phoneDeposit = it.phone_number
                val balanceDouble = it.balance.toDoubleOrNull()
                balance = if (balanceDouble != null && balanceDouble == balanceDouble.toLong().toDouble()) {
                    balanceDouble.toLong().toString()
                } else {
                    it.balance
                }
                Log.d("EditDeposit", "Updated UI fields with data")
            } else {
                Log.e("EditDeposit", "Selected deposit has no valid ID")
            }
        }
    }

    Scaffold(
        topBar = {
            BandTopBar(title = "Ubah deposit", onBack = onNavigateBack)
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
                    text = "Nama Pelanggan",
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
                value = namaDeposit,
                onValueChange = { namaDeposit = it },
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nomor HP",
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
                value = phoneDeposit,
                onValueChange = { phoneDeposit = it },
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Saldo",
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
                value = balance,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        balance = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                leadingIcon = { Text("Rp", modifier = Modifier.padding(start = 8.dp)) },
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
                    if (namaDeposit.isNotBlank() && phoneDeposit.isNotBlank()) {
                        scope.launch {
                            val initialBalance = selectedEditDeposit?.balance?.toDoubleOrNull() ?: 0.0
                            viewModel.updateCustomer(
                                id = customerId,
                                name = namaDeposit.trim(),
                                phoneNumber = phoneDeposit.trim(),
                                initialBalance = balance.toDouble()
                            )
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
                enabled = !isLoading && namaDeposit.isNotBlank() && phoneDeposit.isNotBlank() && balance.isNotBlank()
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

            // Delete lives here, not on the list row, so the row keeps one action besides opening it
            TextButton(
                onClick = { showDelete = true },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Hapus pelanggan ini", style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Hapus pelanggan deposit?",
            message = "Data deposit \u201C${namaDeposit.ifBlank { "pelanggan ini" }}\u201D akan dihapus dan tidak bisa dikembalikan.",
            confirmLabel = "Hapus deposit",
            onDismiss = { showDelete = false },
            onConfirm = { showDelete = false; viewModel.deleteDeposit(customerId) }
        )
    }
}