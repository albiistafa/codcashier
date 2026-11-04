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

// Bug 4 Nov: tapping "Sudah diantar" left no mark on the order. The dialog loaded the order first,
// that load set isLoading, and updateStatus returned early on isLoading: nothing reached the server.
@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryStatusTest {
    private val dispatcher = StandardTestDispatcher()
    private val defaults = mock(TransaksiRepository::class.java)
    private val sent = mutableListOf<TransactionUpdateStatus>()
    private val slowLoad = CompletableDeferred<Result<TransactionGetByIdResponse>>()
    private val order = Transaction(
        created_at = "2026-11-04T10:00:00", id = 10, nama_transaksi = "Meja 3", payment_method = "tunai",
        status = "lunas", is_delivered = "no", total_amount = "12000", transaction_details = emptyList(),
        updated_at = "", user_id = 1
    )
    private val repository = object : TransaksiRepository by defaults {
        override suspend fun getAllTransaksiAcrossPagesNoFilter() =
            Result.success(DataTransaksi(mock(Pagination::class.java), listOf(order)))
        override suspend fun getTransaksiById(id: Int) = slowLoad.await()
        override suspend fun updateStatus(id: Int, updateStatus: TransactionUpdateStatus): Result<TransactionGetByIdResponse> {
            sent += updateStatus
            return Result.success(TransactionGetByIdResponse(200, pendingTransaction(), "OK", true))
        }
    }
    private fun viewModel() = TransaksiViewModel(repository, mock(DepositRepository::class.java),
        mock(SharedPreferences::class.java))

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun deliveryIsSentAndMarkedEvenWhileAnotherLoadRuns() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fetchAllTransaksiNoFilter()
        runCurrent()
        vm.getTransactionById(10) // still loading when the cashier confirms
        runCurrent()
        vm.updateStatus(10, "lunas", "yes", "tunai")
        runCurrent()
        assertEquals(listOf(TransactionUpdateStatus("lunas", "yes")), sent)
        assertEquals("yes", vm.allTransactions.value.single().is_delivered)
        assertTrue(vm.isSuccess.value)
    }

    @Test fun doubleTapSendsOneUpdate() = runTest(dispatcher) {
        val vm = viewModel()
        vm.updateStatus(10, "lunas", "yes", "tunai")
        vm.updateStatus(10, "lunas", "yes", "tunai")
        runCurrent()
        assertEquals(1, sent.size)
    }
}
