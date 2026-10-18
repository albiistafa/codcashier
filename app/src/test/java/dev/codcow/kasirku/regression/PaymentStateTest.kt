package dev.codcow.kasirku.regression

import android.content.SharedPreferences
import dev.codcow.kasirku.core.data.model.transaksi.*
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val defaults = mock(TransaksiRepository::class.java)
    private val response = TransactionGetByIdResponse(200, pendingTransaction(), "OK", true)
    private var calls = 0
    private var sent: TransactionUpdateRequest? = null
    private val completion = CompletableDeferred<Result<TransactionGetByIdResponse>>()
    private val repository = object : TransaksiRepository by defaults {
        override suspend fun getTransaksiById(id: Int) = Result.success(response)
        override suspend fun updateTransaksi(id: Int, updateData: TransactionUpdateRequest): Result<TransactionGetByIdResponse> {
            calls++
            sent = updateData
            return completion.await()
        }
        override suspend fun updateStatus(id: Int, updateStatus: TransactionUpdateStatus) =
            Result.failure<TransactionGetByIdResponse>(IllegalStateException("Tidak tersimpan"))
    }
    private fun viewModel() = TransaksiViewModel(repository, mock(DepositRepository::class.java),
        mock(SharedPreferences::class.java))

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun doubleTapSendsOnePaymentAndWaitsForServer() = runTest(dispatcher) {
        val vm = viewModel()
        vm.getTransactionById(10)
        runCurrent()
        vm.payTransaction("tunai", null)
        vm.payTransaction("digital", null)
        runCurrent()
        assertEquals(1, calls)
        assertEquals("tunai", sent?.payment_method)
        assertEquals("no", sent?.is_delivered)
        assertTrue(vm.isLoading.value)
        assertFalse(vm.isSuccess.value)
        completion.complete(Result.success(response.copy(data = pendingTransaction().copy(status = "lunas", payment_method = "tunai"))))
        runCurrent()
        assertTrue(vm.isSuccess.value)
        assertFalse(vm.isLoading.value)
        assertEquals("lunas", vm.currentTransaction.value?.status)
    }

    @Test fun failedPaymentPreservesPendingOrderAndCanRetry() = runTest(dispatcher) {
        val vm = viewModel()
        vm.getTransactionById(10)
        runCurrent()
        vm.payTransaction("tunai", null)
        completion.complete(Result.failure(IllegalStateException("Koneksi terputus")))
        runCurrent()
        assertFalse(vm.isSuccess.value)
        assertFalse(vm.isLoading.value)
        assertEquals("pending", vm.currentTransaction.value?.status)
        assertEquals("Koneksi terputus", vm.error.value)
        vm.payTransaction("tunai", null)
        runCurrent()
        assertEquals(2, calls)
    }

    @Test fun failedDeliveryUpdateDoesNotReportSuccess() = runTest(dispatcher) {
        val vm = viewModel()
        vm.updateStatus(10, "pending", "yes", null)
        runCurrent()
        assertFalse(vm.isSuccess.value)
        assertFalse(vm.isLoading.value)
        assertEquals("Tidak tersimpan", vm.error.value)
    }
}
