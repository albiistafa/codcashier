package dev.codcow.kasirku.features.transaksi.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePengeluaran(
    modifier: Modifier = Modifier,
    viewModel: TransaksiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var namaNota by remember { mutableStateOf("") }
    var hargaNota by remember { mutableStateOf("") }
    var tanggalPengeluaran by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.pengeluaranSuccess.collectAsState()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val displayFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    val backendFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                // Display date in user-friendly format
                tanggalPengeluaran = selectedDate.format(displayFormatter)
            },
            year,
            month,
            day
        ).show()
    }


    // Handle success state
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Reset the success state
            viewModel.resetState()
            // Navigate back
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Pengeluaran",
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

            // Field Nama Nota
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nama Nota",
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
                value = namaNota,
                onValueChange = { namaNota = it },
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

            // Field Harga Nota
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Harga Nota",
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
                value = hargaNota,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        hargaNota = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                leadingIcon = { Text("Rp", modifier = Modifier.padding(start = 8.dp)) },
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
                    text = "Tanggal Pengeluaran",
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
                value = tanggalPengeluaran,
                onValueChange = { tanggalPengeluaran = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                placeholder = { Text("Masukkan Tanggal Penggeluaran") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AppTheme.colors.surface,
                    unfocusedBorderColor = Color.Gray
                ),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {

                    if (namaNota.isNotBlank() && hargaNota.isNotBlank() && tanggalPengeluaran.isNotBlank()) {
                            val selectedDate = LocalDate.parse(tanggalPengeluaran, displayFormatter)
                            val formattedDate = selectedDate.format(backendFormatter)
                        scope.launch {
                            viewModel.createPengeluaran(
                                namaNota = namaNota,
                                hargaNota = hargaNota.toInt(),
                                tanggalPengeluaran = formattedDate
                            )
                        }
                    } else {

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
                enabled = !isLoading && namaNota.isNotBlank() && hargaNota.isNotBlank() && tanggalPengeluaran.isNotBlank()
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