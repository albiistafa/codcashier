package dev.codcow.kasirku.ui.components

import android.app.DatePickerDialog
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecapFilterSidebar(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onApplyDateFilter: (String, String) -> Unit,
    onResetFilter: () -> Unit = {},
    isPdfLoading: Boolean = false,
    isPdfSuccess: Boolean = false,
    pdfError: String? = null,
    onDownloadPdf: (status: String, paymentMethod: String) -> Unit = { _, _ -> },
    onResetPdfState: () -> Unit = {},
    shadowElevation: Dp = 8.dp
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sidebarWidth = screenWidth * 3 / 4
    val offsetAnimation = animateDpAsState(targetValue = if (isVisible) 0.dp else sidebarWidth, label = "sidebarOffset")
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

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


    var startDate: LocalDate? by remember { mutableStateOf(null) }
    var endDate: LocalDate? by remember { mutableStateOf(null) }

    var tanggalMulaiText by remember { mutableStateOf("") }
    var tanggalSelesaiText by remember { mutableStateOf("") }

    val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
    val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format for API calls

    fun showDatePicker(isStartDate: Boolean) {
        val initialDate = if (isStartDate) startDate else endDate
        val year = initialDate?.year ?: calendar.get(Calendar.YEAR)
        val month = initialDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
        val day = initialDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                if (isStartDate) {
                    startDate = selectedDate
                    tanggalMulaiText = selectedDate.format(displayFormatter)
                } else {
                    endDate = selectedDate
                    tanggalSelesaiText = selectedDate.format(displayFormatter)
                }
            },
            year,
            month,
            day
        ).show()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .offset(x = offsetAnimation.value)
                .graphicsLayer(shadowElevation = shadowElevation.value)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Filter Rekap", style = AppTheme.typography.labelBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pilih Tanggal", style = AppTheme.typography.paragraph2)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Tanggal Mulai", style = AppTheme.typography.paragraph2)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tanggalMulaiText,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Pilih Tanggal Mulai", style = TextStyle(fontSize = 10.sp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(end = 31.dp),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(fontSize = 10.sp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker(true) }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick Start Date")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Tanggal Selesai", style = AppTheme.typography.paragraph2)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tanggalSelesaiText,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Pilih Tanggal Selesai", style = TextStyle(fontSize = 10.sp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(end = 31.dp),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(fontSize = 10.sp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker(false) }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick End Date")
                        }
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = {
                            startDate = null
                            endDate = null
                            tanggalMulaiText = ""
                            tanggalSelesaiText = ""
                            onResetFilter()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF6D8E22)
                        ),
                        border = BorderStroke(1.dp, color = Color(0xFF6D8E22))
                    ) {
                        Text("Atur Ulang", style = AppTheme.typography.paragraph2)
                    }

                    Button(
                        onClick = {
                            val startDateString = startDate?.let {
                                if (it != LocalDate.MIN) it.format(apiFormatter) else ""
                            } ?: ""
                            val endDateString = endDate?.let {
                                if (it != LocalDate.MIN) it.format(apiFormatter) else ""
                            } ?: ""

                            // Pass the formatted dates to the filter function
                            onApplyDateFilter(startDateString, endDateString)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF6D8E22),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Pakai", style = AppTheme.typography.paragraph2)
                    }

                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val startDateString = startDate?.let {
                            if (it != LocalDate.MIN) it.format(apiFormatter) else ""
                        } ?: ""
                        val endDateString = endDate?.let {
                            if (it != LocalDate.MIN) it.format(apiFormatter) else ""
                        } ?: ""

                        if (startDateString.isEmpty() || endDateString.isEmpty()) {
                            onDownloadPdf(startDateString, endDateString)
                        } else {
                            onDownloadPdf(startDateString, endDateString)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF6D8E22),
                        contentColor = Color.White
                    ),
                    enabled = !isPdfLoading
                ) {
                    if (isPdfLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
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
                        color = Color(0xFF6D8E22),
                        style = AppTheme.typography.paragraph2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(visible = pdfError != null) {
                    Text(
                        pdfError ?: "",
                        color = Color.Red,
                        style = AppTheme.typography.paragraph2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}



