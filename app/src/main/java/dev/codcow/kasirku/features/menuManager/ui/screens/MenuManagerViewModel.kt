package dev.codcow.kasirku.features.menuManager.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus

@HiltViewModel
class MenuManagerViewModel @Inject constructor(
    private val menuRepository: MenuRepository,
    private val kategoriRepository: KategoriRepository,
    private val subKategoriRepository: SubKategoriRepository,
    private val cartRepository: CartRepository,
    private val transaksiRepository: TransaksiRepository
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

    fun fetchCategories() {
        viewModelScope.launch {
            kategoriRepository.getAllKategori().onSuccess { fetchedCategories ->
                _categories.value = fetchedCategories
            }.onFailure { exception ->
                _error.value = "Failed to load categories: ${exception.message}"
            }
        }
    }

    fun fetchSubCategories() {
        viewModelScope.launch {
            subKategoriRepository.getAllSubKategori().onSuccess { fetchedSubCategories ->
                _subCategories.value = fetchedSubCategories
            }.onFailure { exception ->
                _error.value = "Failed to load subcategories: ${exception.message}"
            }
        }
    }

    fun fetchMenuItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            menuRepository.getAllMenus().onSuccess { menus ->
                _menuItems.value = menus
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = "Failed to load menu items: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteMenuItem(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = menuRepository.deleteMenu(id)
            println("Delete response: $response")
            response.onSuccess {
                _menuItems.value = _menuItems.value.filterNot { it.id == id }
                println("Item deleted successfully!")
            }.onFailure { exception ->
                _error.value = "Failed to delete menu item: ${exception.message}"
                println("Delete failed: ${exception.message}") // Debugging
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
                        println("Menu created successfully: $createdMenu")
                        _isSuccess.value = true
                        fetchMenuItems()
                    }
                    .onFailure { exception ->
                        println("Failed to create menu: ${exception.message}")
                        _error.value = exception.message ?: "Gagal membuat menu"
                    }
            } catch (e: Exception) {
                println("Exception in createMenu: ${e.message}")
                _error.value = "Terjadi kesalahan: ${e.message}"
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
                        println("Menu created successfully with image: $createdMenu")
                        _isSuccess.value = true
                        fetchMenuItems()
                    }
                    .onFailure { exception ->
                        println("Failed to create menu with image: ${exception.message}")
                        _error.value = exception.message ?: "Gagal membuat menu dengan gambar"
                    }
            } catch (e: Exception) {
                println("Exception in createMenuWithImage: ${e.message}")
                _error.value = "Terjadi kesalahan: ${e.message}"
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
                    _error.value = exception.message
                }
            _isLoading.value = false
        }
    }

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
                _error.value = e.message
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


    fun setPaymentMethod(paymentMethod: String) {
        viewModelScope.launch {
            cartRepository.setSelectedPaymentMethod(paymentMethod)
            _cart.value = cartRepository.getCart()
        }
    }

    fun prepareTransaction(namaTransaksi: String? = null, customerId: Int?): TransactionRequest {
        return runBlocking {
            cartRepository.prepareTransactionData(namaTransaksi, customerId)
        }
    }

    fun prepareTransactionPending(namaTransaksi: String? = null, customerId: Int?): TransactionRequest {
        return runBlocking {
            cartRepository.prepareTransactionDataPending(namaTransaksi, customerId)
        }
    }

    fun searchMenuItems(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            menuRepository.searchMenu(query).onSuccess { searchResult ->
                var filteredMenus = searchResult

                _menuItems.value = filteredMenus
                _isLoading.value = false

            }.onFailure { exception ->
                _error.value = "Search failed: ${exception.message}"
                _isLoading.value = false
            }
        }
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

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                menuRepository.getAllMenus().onSuccess { allMenus ->
                    var filteredItems = allMenus

                    // Filter berdasarkan kategori jika ada kategori yang dipilih
                    if (categoryIds.isNotEmpty()) {
                        filteredItems = filteredItems.filter { menuItem ->
                            categoryIds.contains(menuItem.category_id)
                        }
                    }

                    // Filter berdasarkan subkategori jika ada subkategori yang dipilih
                    if (subCategoryIds.isNotEmpty()) {
                        filteredItems = filteredItems.filter { menuItem ->
                            menuItem.sub_category_id != null && subCategoryIds.contains(menuItem.sub_category_id)
                        }
                    }

                    _menuItems.value = filteredItems
                }.onFailure { exception ->
                    _error.value = "Failed to filter menu: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}