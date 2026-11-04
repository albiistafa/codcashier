package dev.codcow.kasirku.features.menuManager.ui.screens

import dev.codcow.kasirku.ui.util.toUserMessage

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.local.ListCache
import dev.codcow.kasirku.core.data.model.addToCart.Cart
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.model.menu.RequestMenu
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.core.data.repository.CartRepository
import dev.codcow.kasirku.core.domain.repository.KategoriRepository
import dev.codcow.kasirku.core.domain.repository.SubKategoriRepository
import dev.codcow.kasirku.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.core.data.model.addToCart.TransactionRequest
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import org.jetbrains.annotations.ApiStatus

@HiltViewModel
class MenuManagerViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val kategoriRepository: KategoriRepository,
    private val subKategoriRepository: SubKategoriRepository,
    private val cartRepository: CartRepository,
    private val transaksiRepository: TransaksiRepository,
    private val listCache: ListCache
) : ViewModel() {
    private val _menuItems = MutableStateFlow<List<DataMenu>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

    private val _categories = MutableStateFlow<List<DataKategori>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _subCategories = MutableStateFlow<List<DataSubkategori>>(emptyList())
    val subCategories = _subCategories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /** True while the grid shows the copy saved on this phone because the server could not be reached. */
    private val _showingSavedMenu = MutableStateFlow(false)
    val showingSavedMenu = _showingSavedMenu.asStateFlow()

    private val _selectedMenu = MutableStateFlow<DataMenu?>(null)
    val selectedMenu: StateFlow<DataMenu?> = _selectedMenu.asStateFlow()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value

    private val _cart = MutableStateFlow<Cart>(Cart())
    val cart: StateFlow<Cart> = _cart

    private val _itemQuantities = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val itemQuantities: StateFlow<Map<Int, Int>> = _itemQuantities.asStateFlow()

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Saved copy first, server second: the chips are there at once and survive a dropped connection
    fun fetchCategories() {
        viewModelScope.launch {
            if (_categories.value.isEmpty()) listCache.read<DataKategori>(ListCache.CATEGORIES)?.let { _categories.value = it }
            kategoriRepository.getAllKategori().onSuccess { fetchedCategories ->
                _categories.value = fetchedCategories
                listCache.write(ListCache.CATEGORIES, fetchedCategories)
            }.onFailure { exception ->
                if (_categories.value.isEmpty()) _error.value = exception.toUserMessage("Kategori belum bisa dimuat")
            }
        }
    }

    fun fetchSubCategories() {
        viewModelScope.launch {
            if (_subCategories.value.isEmpty()) listCache.read<DataSubkategori>(ListCache.SUBCATEGORIES)?.let { _subCategories.value = it }
            subKategoriRepository.getAllSubKategori().onSuccess { fetchedSubCategories ->
                _subCategories.value = fetchedSubCategories
                listCache.write(ListCache.SUBCATEGORIES, fetchedSubCategories)
            }.onFailure { exception ->
                if (_subCategories.value.isEmpty()) _error.value = exception.toUserMessage("Subkategori belum bisa dimuat")
            }
        }
    }

    private var menuLoadJob: Job? = null
    private var menuLoadVersion = 0
    private var menuQuery = ""

    fun fetchMenuItems() {
        loadMenuItems()
    }

    private fun loadMenuItems(debounce: Boolean = false) {
        val version = ++menuLoadVersion
        menuLoadJob?.cancel()
        val query = menuQuery
        val categoryIds = _selectedCategoryIds.value.toSet()
        val subCategoryIds = _selectedSubCategoryIds.value.toSet()
        _isLoading.value = true
        _error.value = null
        fun List<DataMenu>.shown() = filter { menu ->
            (query.isBlank() || menu.name.contains(query.trim(), ignoreCase = true)) &&
                (categoryIds.isEmpty() || menu.category_id in categoryIds) &&
                (subCategoryIds.isEmpty() || menu.sub_category_id in subCategoryIds)
        }
        menuLoadJob = viewModelScope.launch {
            if (debounce) delay(250)
            // Opening Kasir: show the saved menu straight away, the server answer replaces it
            if (query.isBlank() && _menuItems.value.isEmpty()) {
                listCache.read<DataMenu>(ListCache.MENUS)?.let { _menuItems.value = it.shown() }
            }
            val result = if (query.isBlank()) menuRepository.getAllMenus()
                         else menuRepository.searchMenu(query)
            coroutineContext.ensureActive()
            if (version != menuLoadVersion) return@launch
            result.onSuccess { menus ->
                if (query.isBlank()) listCache.write(ListCache.MENUS, menus)
                _showingSavedMenu.value = false
                // The server already matched the search; only the category chips are applied here
                _menuItems.value = menus.filter { menu ->
                    (categoryIds.isEmpty() || menu.category_id in categoryIds) &&
                        (subCategoryIds.isEmpty() || menu.sub_category_id in subCategoryIds)
                }
            }.onFailure { exception ->
                val saved = listCache.read<DataMenu>(ListCache.MENUS)
                if (saved != null) {
                    // No server: keep selling from the saved menu (search runs on the phone); the server prices the order
                    _menuItems.value = saved.shown()
                    _showingSavedMenu.value = true
                } else {
                    _error.value = exception.toUserMessage("Menu belum bisa dimuat")
                }
            }
            _isLoading.value = false
        }
    }

    fun deleteMenuItem(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = menuRepository.deleteMenu(id)
            response.onSuccess {
                _menuItems.value = _menuItems.value.filterNot { it.id == id }
            }.onFailure { exception ->
                _error.value = exception.toUserMessage("Menu belum terhapus")
                android.util.Log.e("MenuManagerViewModel", "Delete failed", exception)
            }
            _isLoading.value = false
        }
    }

    fun createMenu(menu: RequestMenu) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                menuRepository.createMenu(menu)
                    .onSuccess { createdMenu ->
                        _isSuccess.value = true
                        fetchMenuItems()
                    }
                    .onFailure { exception ->
                        _error.value = exception.toUserMessage("Menu belum tersimpan")
                    }
            } catch (e: Exception) {
                _error.value = e.toUserMessage("Menu belum tersimpan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createMenuWithImage(
        name: String,
        price: String,
        categoryId: Int,
        subCategoryId: Int?,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                menuRepository.createMenuWithImage(name, price, categoryId, subCategoryId, imageFile)
                    .onSuccess { createdMenu ->
                        _isSuccess.value = true
                        fetchMenuItems()
                    }
                    .onFailure { exception ->
                        _error.value = exception.toUserMessage("Menu belum tersimpan")
                    }
            } catch (e: Exception) {
                _error.value = e.toUserMessage("Menu belum tersimpan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMenuById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            menuRepository.getMenuById(id)
                .onSuccess { menuResponse ->
                    _selectedMenu.value = menuResponse
                }
                .onFailure { exception ->
                    _error.value = exception.toUserMessage("Menu belum bisa dibuka")
                }
            _isLoading.value = false
        }
    }

    suspend fun photoForMenu(id: Int): String? =
        _menuItems.value.firstOrNull { it.id == id }?.photo
            ?: menuRepository.getMenuById(id).getOrNull()?.photo

    fun updateMenu(
        id: Int,
        name: String,
        price: String,
        categoryId: Int,
        subCategoryId: Int?,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                menuRepository.updateMenu(id, name, price, categoryId, subCategoryId, imageFile)
                _isSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.toUserMessage("Perubahan menu belum tersimpan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    init {
        viewModelScope.launch {
            cartRepository.getCart().also { _cart.value = it }
        }
    }

    fun getItemQuantity(menuId: Int): Int {
        return _cart.value.items.find { it.menuId == menuId }?.quantity ?: 0
    }

    fun addToCart(menu: DataMenu, quantity: Int) {
        viewModelScope.launch {
            cartRepository.addToCart(
                CartItem(
                    menuId = menu.id,
                    quantity = quantity,
                    name = menu.name,
                    price = menu.price,
                    photo = menu.photo
                )
            )
            _cart.value = cartRepository.getCart() // Refresh cart state
        }
    }

    fun removeFromCart(menuId: Int) {
        viewModelScope.launch {
            cartRepository.removeFromCart(menuId)
            _cart.value = cartRepository.getCart() // Refresh cart state
        }
    }

    // Update quantity
    fun updateCartItemQuantity(menuId: Int, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateItemQuantity(menuId, quantity)
            _cart.value = cartRepository.getCart() // Refresh cart state
        }
    }

    fun clearCartAndWait() {
        viewModelScope.launch {
            cartRepository.clearCart()
            // Set cart state langsung ke Cart kosong
            _cart.value = Cart(
                items = emptyList(),
                totalPrice = 0.0,
                customerId = null,
                paymentMethod = null
            )
        }
    }

    fun refreshCart() {
        viewModelScope.launch {
            _cart.value = cartRepository.getCart()
        }
    }


    suspend fun setPaymentMethod(paymentMethod: String?) {
        cartRepository.setSelectedPaymentMethod(paymentMethod)
        _cart.value = cartRepository.getCart()
    }

    suspend fun prepareTransaction(namaTransaksi: String? = null, customerId: Int?): TransactionRequest =
        cartRepository.prepareTransactionData(namaTransaksi, customerId)

    suspend fun prepareTransactionPending(namaTransaksi: String? = null, customerId: Int?): TransactionRequest =
        cartRepository.prepareTransactionDataPending(namaTransaksi, customerId)

    fun searchMenuItems(query: String) {
        menuQuery = query
        loadMenuItems(debounce = query.isNotBlank())
    }

    // Tambahkan state untuk kategori yang dipilih
    private val _selectedCategoryIds = MutableStateFlow<List<Int>>(emptyList())
    val selectedCategoryIds = _selectedCategoryIds.asStateFlow()

    private val _selectedSubCategoryIds = MutableStateFlow<List<Int>>(emptyList())
    val selectedSubCategoryIds = _selectedSubCategoryIds.asStateFlow()

    // Fungsi filter berdasarkan kategori dan subkategori
    fun filterMenuItems(categoryIds: List<Int>, subCategoryIds: List<Int>) {
        _selectedCategoryIds.value = categoryIds
        _selectedSubCategoryIds.value = subCategoryIds
        loadMenuItems()
    }
}
