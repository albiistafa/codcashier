package dev.codcow.kasirku.ui.components

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterTransition
import dev.codcow.kasirku.ui.util.rememberReduceMotion
import android.util.Log
import android.util.Log.e
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay

data class FilterItem(
    val id: String,
    val label: String
)

@Composable
fun FilterSideTransaksi(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onResetFilter: () -> Unit = {},
    searchQuery: String = "",
    onApplyFilter: (query: String, paymentMethod: String, status: String) -> Unit = { _, _, _ -> },
    isPdfLoading: Boolean = false,
    isPdfSuccess: Boolean = false,
    pdfError: String? = null,
    onDownloadPdf: (status: String, paymentMethod: String) -> Unit = { _, _ -> },
    onResetPdfState: () -> Unit = {},
    shadowElevation: Dp = 8.dp
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sidebarWidth = screenWidth * 3 / 4

    // Define payment statuses
    val paymentStatuses = listOf(
        FilterItem("pending", "Pending"),
        FilterItem("lunas", "Lunas")
    )

    // Define payment methods
    val paymentMethods = listOf(
        FilterItem("digital", "Digital"),
        FilterItem("tunai", "Tunai"),
        FilterItem("deposit", "Deposit")
    )

    val selectedStatusId = remember { mutableStateOf<String?>(null) }
    val selectedMethodId = remember { mutableStateOf<String?>(null) }
    val currentSearchQuery = remember { mutableStateOf(searchQuery) }

    LaunchedEffect(isPdfSuccess, pdfError) {
        if (isPdfSuccess) {
            // Reset PDF state after showing success
            delay(3000)
            onResetPdfState()
        }

        if (pdfError != null) {
            // Reset PDF state after showing error
            delay(3000)
            onResetPdfState()
        }
    }

    val reduceMotion = rememberReduceMotion()
    AnimatedVisibility(
        visible = isVisible,
        enter = if (reduceMotion) EnterTransition.None else slideInHorizontally(initialOffsetX = { it }),
        exit = if (reduceMotion) ExitTransition.None else slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .graphicsLayer(shadowElevation = shadowElevation.value)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Filter Transaksi", style = AppTheme.typography.labelBold)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Status Pembayaran", style = AppTheme.typography.paragraph2)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(80.dp),
                    content = {
                        items(paymentStatuses.chunked(2)) { rowStatuses ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                rowStatuses.forEach { status ->
                                    FilterChip(
                                        selected = selectedStatusId.value == status.id,
                                        onClick = {
                                            selectedStatusId.value = if (selectedStatusId.value == status.id) null else status.id
                                        },
                                        label = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                            ) {
                                                Text(
                                                    status.label,
                                                    style = AppTheme.typography.paragraph2,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color.Transparent,
                                            borderWidth = 0.dp,
                                            enabled = true,
                                            selected = true
                                        )
                                    )
                                }
                                repeat(2 - rowStatuses.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Metode Pembayaran", style = AppTheme.typography.paragraph2)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    content = {
                        items(paymentMethods.chunked(2)) { rowMethods ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                rowMethods.forEach { method ->
                                    FilterChip(
                                        selected = selectedMethodId.value == method.id,
                                        onClick = {
                                            selectedMethodId.value = if (selectedMethodId.value == method.id) null else method.id
                                        },
                                        label = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                            ) {
                                                Text(
                                                    method.label,
                                                    style = AppTheme.typography.paragraph2,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color.Transparent,
                                            borderWidth = 0.dp,
                                            enabled = true,
                                            selected = true
                                        )
                                    )
                                }
                                repeat(2 - rowMethods.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            selectedStatusId.value = null
                            selectedMethodId.value = null
                            currentSearchQuery.value = ""
                            onResetFilter()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Atur Ulang", style = AppTheme.typography.paragraph2)
                    }

                    Button(
                        onClick = {
                            onApplyFilter(
                                currentSearchQuery.value,
                                selectedMethodId.value ?: "",
                                selectedStatusId.value ?: ""
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Terapkan", style = AppTheme.typography.paragraph2)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val status = selectedStatusId.value ?: ""
                        Log.d("Apply status", "status selected: $selectedStatusId")
                        val paymentMethod = selectedMethodId.value ?: ""
                        Log.d("Apply method", "method selected: $selectedMethodId")
                        onDownloadPdf(status, paymentMethod)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isPdfLoading
                ) {
                    if (isPdfLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Unduh PDF", style = AppTheme.typography.paragraph2)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PDF download status messages
                AnimatedVisibility(visible = isPdfSuccess) {
                    Text(
                        "PDF berhasil diunduh!",
                        color = MaterialTheme.colorScheme.primary,
                        style = AppTheme.typography.paragraph2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(visible = pdfError != null) {
                    Text(
                        pdfError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = AppTheme.typography.paragraph2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
