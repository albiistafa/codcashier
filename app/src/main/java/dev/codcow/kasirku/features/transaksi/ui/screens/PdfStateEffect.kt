package dev.codcow.kasirku.features.transaksi.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel.PdfState

/**
 * Opens a ready PDF or reports why it could not, then hands the state back to Idle.
 * Shared by Transaksi and Rekap so both screens fail the same, visible way.
 */
@Composable
fun PdfStateEffect(pdf: PdfState, snackbarHostState: SnackbarHostState, onHandled: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(pdf) {
        val message = when (pdf) {
            is PdfState.Ready -> when {
                !pdf.file.exists() || pdf.file.length() == 0L ->
                    "PDF belum bisa dibuka karena isinya kosong. Coba unduh lagi."
                else -> try {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdf.file)
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    null
                } catch (e: ActivityNotFoundException) {
                    "PDF sudah dibuat, tapi belum ada aplikasi pembuka PDF di HP ini. Pasang aplikasinya, lalu unduh lagi."
                } catch (e: Exception) {
                    "PDF sudah dibuat, tapi belum bisa dibuka. Coba unduh lagi."
                }
            }
            is PdfState.Failed -> pdf.message
            PdfState.Idle, PdfState.Loading -> return@LaunchedEffect
        }
        // Snackbar first: going Idle restarts this effect and would cancel the message mid-show
        message?.let { snackbarHostState.showSnackbar(it) }
        onHandled()
    }
}
