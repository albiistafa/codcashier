package dev.codcow.kasirku.regression

import dev.codcow.kasirku.core.data.local.ListCache
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
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MenuCacheTest {
    @get:Rule val tmp = TemporaryFolder()
    private val dispatcher = StandardTestDispatcher()
    private val cart = mock(CartRepository::class.java)
    private val defaults = mock(MenuRepository::class.java)
    private var server = CompletableDeferred<Result<List<DataMenu>>>()
    private val repository = object : MenuRepository by defaults {
        override suspend fun getAllMenus() = server.await()
    }
    private fun menu(id: Int, name: String = "Menu $id") = DataMenu(id, name, "1000", 1, null, null, null, null)
    private fun cache() = ListCache(tmp.root, dispatcher)
    private fun viewModel() = MenuManagerViewModel(repository, mock(KategoriRepository::class.java),
        mock(SubKategoriRepository::class.java), cart, mock(TransaksiRepository::class.java), cache())

    @Before fun setup() = runBlocking {
        Dispatchers.setMain(dispatcher)
        `when`(cart.getCart()).thenReturn(Cart())
        Unit
    }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun savedMenuShowsAtOnceThenServerReplacesIt() = runTest(dispatcher) {
        cache().write(ListCache.MENUS, listOf(menu(1)))
        val vm = viewModel()
        vm.fetchMenuItems()
        runCurrent()
        assertEquals(listOf(1), vm.menuItems.value.map { it.id }) // before the server answered
        assertTrue(vm.isLoading.value)
        server.complete(Result.success(listOf(menu(1), menu(2))))
        runCurrent()
        assertEquals(listOf(1, 2), vm.menuItems.value.map { it.id })
        assertFalse(vm.showingSavedMenu.value)
        assertEquals(listOf(1, 2), cache().read<DataMenu>(ListCache.MENUS)?.map { it.id })
    }

    @Test fun noConnectionKeepsSellingFromSavedMenu() = runTest(dispatcher) {
        cache().write(ListCache.MENUS, listOf(menu(1, "Soto"), menu(2, "Es teh")))
        val vm = viewModel()
        vm.fetchMenuItems()
        server.complete(Result.failure(IOException("no signal")))
        runCurrent()
        assertEquals(listOf(1, 2), vm.menuItems.value.map { it.id })
        assertTrue(vm.showingSavedMenu.value)
        assertNull(vm.error.value) // the grid stays, no full-screen error
    }

    @Test fun firstRunWithoutConnectionStillSaysWhatFailed() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fetchMenuItems()
        server.complete(Result.failure(IOException("no signal")))
        runCurrent()
        assertTrue(vm.menuItems.value.isEmpty())
        assertTrue(vm.error.value!!.startsWith("Menu belum bisa dimuat."))
    }

    @Test fun clearForgetsTheSavedMenu() = runTest(dispatcher) {
        cache().write(ListCache.MENUS, listOf(menu(1)))
        cache().clear()
        assertNull(cache().read<DataMenu>(ListCache.MENUS))
    }
}
