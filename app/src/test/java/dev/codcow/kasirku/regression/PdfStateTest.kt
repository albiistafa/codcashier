package dev.codcow.kasirku.regression

import android.content.SharedPreferences
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel.PdfState
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.mock
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PdfStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val defaults = mock(TransaksiRepository::class.java)
    private var calls = 0
    private val pdf = CompletableDeferred<Result<ByteArray>>()
    private val repository = object : TransaksiRepository by defaults {
        override suspend fun downloadRekapPdf(startDate: String, endDate: String, type: String): Result<ByteArray> {
            calls++
            return pdf.await()
        }
    }
    private fun viewModel() = TransaksiViewModel(repository, mock(DepositRepository::class.java),
        mock(SharedPreferences::class.java))

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun doubleTapDownloadsOnceThenReady() = runTest(dispatcher) {
        val vm = viewModel()
        vm.downloadRekapPdf("2026-11-01", "2026-11-04", "pemasukan", "rekap")
        vm.downloadRekapPdf("2026-11-01", "2026-11-04", "pemasukan", "rekap")
        runCurrent()
        assertEquals(1, calls)
        assertEquals(PdfState.Loading, vm.pdf.value)
        pdf.complete(Result.success(byteArrayOf(1, 2, 3)))
        runCurrent()
        val ready = vm.pdf.value as PdfState.Ready
        assertEquals(3L, ready.file.length())
        ready.file.delete()
        vm.resetPdfState()
        assertEquals(PdfState.Idle, vm.pdf.value)
    }

    @Test fun failureIsOneStateWithARecoveryMessage() = runTest(dispatcher) {
        val vm = viewModel()
        vm.downloadRekapPdf("2026-11-01", "2026-11-04", "pemasukan", "rekap")
        pdf.complete(Result.failure(IOException("timeout")))
        runCurrent()
        val failed = vm.pdf.value as PdfState.Failed
        assertTrue(failed.message.startsWith("PDF belum bisa dibuat. Koneksi internet"))
    }
}
