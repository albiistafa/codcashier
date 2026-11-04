package dev.codcow.kasirku.regression

import android.content.SharedPreferences
import com.google.gson.JsonSyntaxException
import dev.codcow.kasirku.core.data.model.SearchRekap.DataSearch
import dev.codcow.kasirku.core.data.model.pemasukan.DataPemasukan
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionPemasukan
import dev.codcow.kasirku.core.data.model.transaksi.Pagination
import dev.codcow.kasirku.core.data.model.pengeluaran.DataPengeluaran
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranRequest
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranResponse
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiViewModel.RekapPeriod
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.mock
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RekapPeriodTest {
    private val dispatcher = StandardTestDispatcher()
    private val defaults = mock(TransaksiRepository::class.java)
    private val searches = mutableListOf<String>()
    private val pengeluaranLoads = mutableListOf<RekapPeriod>()
    private val repository = object : TransaksiRepository by defaults {
        override suspend fun searchRekapAcrossPages(query: String, startDate: String, endDate: String, type: String) =
            Result.failure<DataSearch>(IllegalStateException("offline")).also { searches += "$type:$query" }
        override suspend fun getPengeluaranAcrossPage(startDate: String, endDate: String): Result<DataPengeluaran> {
            pengeluaranLoads += RekapPeriod(startDate, endDate)
            return Result.failure(IllegalStateException("offline"))
        }
        override suspend fun createPengeluaran(request: PengeluaranRequest) =
            Result.success(mock(PengeluaranResponse::class.java))
    }
    private fun viewModel() = TransaksiViewModel(repository, mock(DepositRepository::class.java),
        mock(SharedPreferences::class.java))

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun eachTabKeepsItsOwnPeriodAndSearchRunsOnce() = runTest(dispatcher) {
        val vm = viewModel()
        vm.searchRekapPengeluaran("gas", "2026-11-01", "2026-11-04")
        advanceTimeBy(301); runCurrent() // past the typing pause
        assertEquals(RekapPeriod("2026-11-01", "2026-11-04"), vm.pengeluaranPeriod.value)
        assertEquals(RekapPeriod(), vm.pemasukanPeriod.value)
        assertEquals(listOf("pengeluaran:gas"), searches)
    }

    // Bug 4 Nov: filter 2 days back + search, leave Rekap, come back on a day with no income:
    // the empty day fails to parse, and the old search results (and their total) stayed on screen.
    @Test fun emptyDayAfterASearchShowsNothingFromTheSearch() = runTest(dispatcher) {
        val found = mock(TransactionPemasukan::class.java)
        var day: Result<DataPemasukan> = Result.success(DataPemasukan(mock(Pagination::class.java), listOf(found)))
        val vm = TransaksiViewModel(object : TransaksiRepository by repository {
            override suspend fun getPemasukanAcrossPage(startDate: String, endDate: String) = day
        }, mock(DepositRepository::class.java), mock(SharedPreferences::class.java))
        vm.fetchAllPemasukanAcrossPages("2026-11-02", "2026-11-02")
        runCurrent()
        assertEquals(listOf(found), vm.allPemasukan.value)

        day = Result.failure(JsonSyntaxException("data is [] on a day without income"))
        vm.fetchAllPemasukanAcrossPages("", "")
        runCurrent()
        assertEquals(emptyList<TransactionPemasukan>(), vm.allPemasukan.value)
        assertEquals("Tidak ada data pemasukan", vm.errorPemasukan.value)
    }

    // Same bug, second path: the search started on the old period was still on its way when Rekap
    // reopened, and landed after today's (empty) load. Reopening must cancel it.
    @Test fun slowOldSearchCannotLandAfterReopening() = runTest(dispatcher) {
        val slowSearch = CompletableDeferred<Result<DataSearch>>()
        val vm = TransaksiViewModel(object : TransaksiRepository by repository {
            override suspend fun searchRekapAcrossPages(query: String, startDate: String, endDate: String, type: String) =
                slowSearch.await()
            override suspend fun getPemasukanAcrossPage(startDate: String, endDate: String) =
                Result.failure<DataPemasukan>(JsonSyntaxException("data is [] on a day without income"))
        }, mock(DepositRepository::class.java), mock(SharedPreferences::class.java))
        vm.searchRekapPemasukan("es teh", "2026-11-02", "2026-11-02")
        advanceTimeBy(301); runCurrent() // past the typing pause
        vm.fetchAllPemasukanAcrossPages("", "")
        runCurrent()
        slowSearch.complete(Result.failure(IOException("old search answered late")))
        runCurrent()
        assertEquals("Tidak ada data pemasukan", vm.errorPemasukan.value)
        assertNotEquals("2026-11-02", vm.pemasukanPeriod.value.start)
    }

    @Test fun fastTypingSendsOneSearchForTheLastWord() = runTest(dispatcher) {
        val vm = viewModel()
        listOf("e", "es", "es t").forEach { vm.searchRekapPemasukan(it, "2026-11-04", "2026-11-04"); advanceTimeBy(100) }
        advanceTimeBy(301); runCurrent()
        assertEquals(listOf("pemasukan:es t"), searches)
    }

    @Test fun newExpenseReloadsTheShownPeriod() = runTest(dispatcher) {
        val vm = viewModel()
        vm.searchRekapPengeluaran("gas", "2026-11-01", "2026-11-04")
        advanceTimeBy(301); runCurrent() // past the typing pause
        vm.createPengeluaran("Gas elpiji", 22000, "2026-11-03")
        runCurrent()
        assertEquals(listOf(RekapPeriod("2026-11-01", "2026-11-04")), pengeluaranLoads)
    }
}
