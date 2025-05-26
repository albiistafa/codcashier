package dev.codcow.kasirku.features.transaksi.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import captureComposableToBitmap
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.core.data.model.warung.Data
import dev.codcow.kasirku.features.pdf.ReceiptContent
import dev.codcow.kasirku.features.pdf.createReceiptBitmap
import dev.codcow.kasirku.features.pdf.saveBitmapAsPdf
import dev.codcow.kasirku.features.warung.ui.screens.warungViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.TransactionReceipt
import dev.codcow.kasirku.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPembayaran(
    navController: NavController,
    transactionId: Int,
    transaksiViewModel: TransaksiViewModel = hiltViewModel(),
    warungViewModel: warungViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val warungName by warungViewModel.warung.collectAsState()
    val isLoading by transaksiViewModel.isLoading.collectAsState()
    val error by transaksiViewModel.error.collectAsState()
    val transaction by transaksiViewModel.currentTransaction.collectAsState()
    val context = LocalContext.current

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("DetailPembayaran", "Screen initialized with transactionId: $transactionId")
    }

    LaunchedEffect(warungName) {
        Log.d("DetailPembayaran", "Warung name: ${warungName?.name ?: "null"}")
    }

    LaunchedEffect(transactionId) {
        Log.d("DetailPembayaran", "Fetching transaction with ID: $transactionId")
        transaksiViewModel.getTransactionById(transactionId)
    }

    LaunchedEffect(transaction) {
        Log.d("DetailPembayaran", "Transaction updated: ${transaction != null}")
        transaction?.let {
            Log.d("DetailPembayaran", "Transaction details: id=${it.id}, total=${it.total_amount}, method=${it.payment_method}")
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Log.e("DetailPembayaran", "Error occurred: $it")
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(isLoading) {
        Log.d("DetailPembayaran", "Loading state changed: $isLoading")
    }

    // Prevent auto-navigation when loading
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF90C14F))
        }
        return
    }

    // Show error state
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    style = AppTheme.typography.paragraph1
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigateUp() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6D8E22)
                    )
                ) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Detail Pembayaran",
                            style = AppTheme.typography.labelBold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6D8E22)
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = Color(0xFF6D8E22))
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // You might want to display some header information here based on the transaction
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = (-80).dp) // Angkat ke atas supaya kelihatan "numpuk"
                        .zIndex(2f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center // Center content when loading/error
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF90C14F))
                    } else if (error != null) {
                        Text(
                            text = "Error: ${error}",
                            color = Color.Red,
                            style = AppTheme.typography.paragraph1
                        )
                    } else {
                        // Display Transaction Receipt when data is loaded
                        transaction?.let { currentTransaction ->
                            TransactionReceipt(
                                cartItems = currentTransaction.transaction_details?.map {
                                    CartItem(
                                        menuId = it.menu_id ?: 0,
                                        name = it.menu_name ?: "",
                                        quantity = it.quantity ?: 0,
                                        price = (it.menu_price?.toDouble() ?: 0.0).toString(),
                                        photo = null
                                    )
                                } ?: emptyList(),
                                totalBiaya = currentTransaction.total_amount?.toDouble() ?: 0.0,
                                paymentMethod = currentTransaction.payment_method,
                                customerName = currentTransaction.nama_transaksi
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxSize()
//                        .background(Color(0xFFFFFFFF))
                    ,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {

                }

            }

            // TOMBOL DI LUAR CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFFFF))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            transaction?.let { currentTransaction ->
                                try {
                                    Log.d("PDF_DEBUG", "Creating receipt bitmap directly")
                                    // Use the alternative approach without Compose
                                    val bitmap = createReceiptBitmap(context, currentTransaction, warungName
                                    )

                                    val savedUri = saveBitmapAsPdf(context, bitmap)
                                    if (savedUri != null) {
                                        Log.d("PDF_DEBUG", "PDF saved successfully: $savedUri")
                                        Toast.makeText(context, "Struk berhasil diunduh", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("PDF_DEBUG", "Failed to save PDF, URI is null")
                                        Toast.makeText(context, "Gagal mengunduh struk", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("PDF_DEBUG", "Exception during PDF creation: ${e.message}", e)
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } ?: run {
                                Log.e("PDF_DEBUG", "transaction is null")
                                Toast.makeText(context, "Data transaksi tidak tersedia", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ,
                        border = BorderStroke(1.dp, Color(0xFF6D8E22)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && transaction != null // Enable button only when data is loaded
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Unduh",
                            tint = Color(0xFF6D8E22),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Print",
                            color = Color(0xFF6D8E22)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { navController.navigate(Screen.Menu.route) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6D8E22),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && transaction != null // Enable button only when data is loaded
                    ) {
                        Text("Kembali")
                    }
                }
            }
        }
    }
}