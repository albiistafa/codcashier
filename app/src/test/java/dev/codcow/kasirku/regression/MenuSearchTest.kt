package dev.codcow.kasirku.regression

import dev.codcow.kasirku.core.data.model.addToCart.Cart
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.repository.CartRepository
import dev.codcow.kasirku.core.domain.repository.*
import dev.codcow.kasirku.data.repository.MenuRepository
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuManagerViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class MenuSearchTest {
    private val dispatcher = StandardTestDispatcher()
    private val cart = mock(CartRepository::class.java)
    private val defaults = mock(MenuRepository::class.java)
    private val requests = mutableListOf<String>()
    private val pending = mutableMapOf<String, Continuation<Result<List<DataMenu>>>>()
    private val repository = object : MenuRepository by defaults {
        override suspend fun searchMenu(query: String): Result<List<DataMenu>> {
            requests += query
            // Model an API adapter that delivers a response even after cancellation.
            return suspendCoroutine { pending[query] = it }
        }
        override suspend fun getAllMenus() = Result.success(listOf(menu(3, 1), menu(4, 2)))
    }
    private fun menu(id: Int, category: Int = 1) = DataMenu(id, "Menu $id", "1000", category, null, null, null, null)
    private fun viewModel() = MenuManagerViewModel(repository, mock(KategoriRepository::class.java),
        mock(SubKategoriRepository::class.java), cart, mock(TransaksiRepository::class.java))

    @Before fun setup() = runBlocking {
        Dispatchers.setMain(dispatcher)
        `when`(cart.getCart()).thenReturn(Cart())
        Unit
    }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun staleResponseCannotOverwriteNewestResults() = runTest(dispatcher) {
        val vm = viewModel()
        vm.searchMenuItems("s")
        advanceTimeBy(250); runCurrent()
        vm.searchMenuItems("soto")
        advanceTimeBy(250); runCurrent()
        pending.getValue("soto").resume(Result.success(listOf(menu(2))))
        runCurrent()
        pending.getValue("s").resume(Result.success(listOf(menu(1))))
        runCurrent()
        assertEquals(listOf(2), vm.menuItems.value.map { it.id })
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
    }

    @Test fun clearingSearchRetainsFiltersAndIgnoresOldFailure() = runTest(dispatcher) {
        val vm = viewModel()
        vm.filterMenuItems(listOf(2), emptyList())
        runCurrent()
        vm.searchMenuItems("lama")
        advanceTimeBy(250); runCurrent()
        vm.searchMenuItems("")
        runCurrent()
        pending.getValue("lama").resume(Result.failure(Exception("old error")))
        runCurrent()
        assertEquals(listOf(4), vm.menuItems.value.map { it.id })
        assertNull(vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test fun fastTypingOnlySendsFinalQuery() = runTest(dispatcher) {
        val vm = viewModel()
        vm.searchMenuItems("s"); advanceTimeBy(100)
        vm.searchMenuItems("so"); advanceTimeBy(100)
        vm.searchMenuItems("soto"); advanceTimeBy(250); runCurrent()
        assertEquals(listOf("soto"), requests)
        pending.getValue("soto").resume(Result.success(emptyList()))
        runCurrent()
        assertTrue(vm.menuItems.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }
}
