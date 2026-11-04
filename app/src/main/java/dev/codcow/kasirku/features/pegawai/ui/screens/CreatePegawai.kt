package dev.codcow.kasirku.features.pegawai.ui.screens
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.codcow.kasirku.core.data.model.pegawai.CreatePegawaiRequest
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePegawai(
    modifier: Modifier = Modifier,
    viewModel: PegawaiManagerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var namaPegawai by remember { mutableStateOf("") }
    var nomorPegawai by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var konfirmasiPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val scope = rememberCoroutineScope()

    // Handle success state
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Reset the success state
            viewModel.resetSuccessState()
            // Navigate back
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            BandTopBar(title = "Tambah pegawai", onBack = onNavigateBack)
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

            // Field Nama Pegawai
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nama Pegawai",
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
                value = namaPegawai,
                onValueChange = {
                  namaPegawai = it
                },
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

            // Field Nomor Pegawai
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
                value = nomorPegawai,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        nomorPegawai = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                supportingText = { Text("Dipakai pegawai untuk masuk ke Kasirku", style = AppTheme.typography.paragraph2) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Field Password
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Password",
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
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Field Konfirmasi Password
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ulangi password",
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
                value = konfirmasiPassword,
                onValueChange = { konfirmasiPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                isError = passwordError != null
            )

            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = AppTheme.typography.paragraph2,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Simpan di bagian bawah
            Button(
                onClick = {
                    // Validate fields
                    if (password != konfirmasiPassword) {
                        passwordError = "Password belum sama. Ketik ulang password yang sama."
                        return@Button
                    } else {
                        passwordError = null
                    }

                    if (namaPegawai.isNotBlank() && nomorPegawai.isNotBlank() && password.isNotBlank()) {
                        scope.launch {
                            val request = CreatePegawaiRequest(
                                name = namaPegawai.trim(),
                                phone_number = nomorPegawai.trim(),
                                role = "pegawai",
                                password = password
                            )
                            viewModel.createPegawai(request)
                        }
                    } else {
                        viewModel.setError("Semua field harus diisi")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = !isLoading && namaPegawai.isNotBlank() && nomorPegawai.isNotBlank()
                        && password.isNotBlank() && konfirmasiPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Simpan pegawai",
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

            // Error from local validation
            viewModel.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = AppTheme.typography.paragraph2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
