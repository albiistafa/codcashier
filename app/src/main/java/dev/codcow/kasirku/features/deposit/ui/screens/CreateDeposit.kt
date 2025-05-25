package dev.codcow.kasirku.features.deposit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun CreateDeposit(
    modifier: Modifier = Modifier,
    viewModel: DepositViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    navController: NavController
) {
    var namaDeposit by remember { mutableStateOf("") }
    var phoneDeposit by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val scope = rememberCoroutineScope()

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
                        text = "Catat Deposit Customer",
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
                    text = "Nama Pelanggan",
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
                value = namaDeposit,
                onValueChange = { namaDeposit = it },
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

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nomor HP",
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
                value = phoneDeposit,
                onValueChange = { phoneDeposit = it },
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

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Saldo",
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AppTheme.colors.surface,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Simpan di bagian bawah
            Button(
                onClick = {
                    if (namaDeposit.isNotBlank()
                        && phoneDeposit.isNotBlank()
                        && balance.isNotBlank()) {
                        scope.launch {
                            val request = CreateDataDeposit(
                                customer_name = namaDeposit.trim(),
                                phone = phoneDeposit.trim(),
                                balance = balance.trim()
                            )
                            viewModel.createDeposit(request).collect { result ->
                                when (result) {
                                    is Result.Success -> {
                                        onNavigateBack()
                                    }
                                    is Result.Error -> {

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
                    containerColor = AppTheme.colors.surface,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isLoading && namaDeposit.isNotBlank() && phoneDeposit.isNotBlank() && balance.isNotBlank()
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