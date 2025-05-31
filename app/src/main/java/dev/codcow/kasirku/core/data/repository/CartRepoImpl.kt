package dev.codcow.kasirku.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import dev.codcow.kasirku.core.data.model.addToCart.Cart
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.core.data.model.addToCart.CartItemRequest
import dev.codcow.kasirku.core.data.model.addToCart.TransactionRequest
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(private val context: Context) {

    private object CartSerializer : Serializer<Cart> {
        override val defaultValue: Cart = Cart()

        override suspend fun readFrom(input: InputStream): Cart {
            return try {
                Json.decodeFromString(input.readBytes().decodeToString())
            } catch (e: Exception) {
                defaultValue
            }
        }

        override suspend fun writeTo(t: Cart, output: OutputStream) {
            output.write(Json.encodeToString(t).encodeToByteArray())
        }
    }

    // Buat extension function untuk Context yang menyediakan DataStore
    private val Context.cartDataStore: DataStore<Cart> by dataStore(
        fileName = "cart_data.json",
        serializer = CartSerializer
    )

    // Akses DataStore melalui context
    private val dataStore = context.cartDataStore

    private val _cartState = MutableStateFlow<Cart>(Cart())
    val cartState: StateFlow<Cart> = _cartState

    // Get current cart as Flow
    val cartFlow: Flow<Cart> = dataStore.data
        .catch { exception ->
            emit(Cart())
        }
        .onEach { cart ->
            // Update _cartState setiap kali ada perubahan di DataStore
            _cartState.value = cart
        }

    // Get current cart
    suspend fun getCart(): Cart {
        return cartFlow.first()
    }

    // Update cart
    suspend fun updateCart(cart: Cart) {
        val updatedCart = cart.copy(totalPrice = cart.items.sumOf { it.price.toDouble() * it.quantity })
        dataStore.updateData { updatedCart }
        _cartState.value = updatedCart
    }

    // Add item to cart
    suspend fun addToCart(item: CartItem) {
        dataStore.updateData { currentCart ->
            val existingItem = currentCart.items.find { it.menuId == item.menuId }
            val updatedItems = if (existingItem != null) {
                currentCart.items.map {
                    if (it.menuId == item.menuId) {
                        it.copy(quantity = it.quantity + item.quantity)
                    } else {
                        it
                    }
                }
            } else {
                currentCart.items + item
            }
            val updatedCart = currentCart.copy(items = updatedItems, totalPrice = updatedItems.sumOf { it.price.toDouble() * it.quantity })
            _cartState.value = updatedCart
            updatedCart
        }
    }

    // Remove item from cart
    suspend fun removeFromCart(menuId: Int) {
        dataStore.updateData { currentCart ->
            val updatedItems = currentCart.items.filter { it.menuId != menuId }
            val updatedCart = currentCart.copy(
                items = updatedItems,
                totalPrice = updatedItems.sumOf { it.price.toDouble() * it.quantity })
            _cartState.value = updatedCart
            updatedCart
        }
    }

    suspend fun updateItemQuantity(menuId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(menuId)
            return
        }
        dataStore.updateData { currentCart ->
            val updatedItems = currentCart.items.map {
                if (it.menuId == menuId) {
                    it.copy(quantity = quantity)
                } else {
                    it
                }
            }
            val updatedCart = currentCart.copy(items = updatedItems, totalPrice = updatedItems.sumOf { it.price.toDouble() * it.quantity })
            _cartState.value = updatedCart
            updatedCart
        }
    }

    suspend fun clearCart() {
        val emptyCart = Cart(
            items = emptyList(),
            totalPrice = 0.0,
            customerId = null,
            paymentMethod = null
        )
        dataStore.updateData { emptyCart }
        _cartState.value = emptyCart
        _selectedPaymentMethod.value = null
    }

    suspend fun setCustomerId(customerId: Int?) {
        dataStore.updateData { it.copy(customerId = customerId, totalPrice = it.items.sumOf { item -> item.price.toDouble() * item.quantity }) }
    }

    private val _selectedPaymentMethod = MutableStateFlow<String?>(null)
    val selectedPaymentMethod: StateFlow<String?> = _selectedPaymentMethod

    suspend fun setSelectedPaymentMethod(paymentMethod: String?) {
        _selectedPaymentMethod.value = paymentMethod
        dataStore.updateData { it.copy(paymentMethod = paymentMethod, totalPrice = it.items.sumOf { item -> item.price.toDouble() * item.quantity }) }
    }



    // Prepare transaction data for API
    suspend fun prepareTransactionData(namaTransaksi: String? = null, customerId: Int? = null): TransactionRequest {
        val cart = getCart()
        val currentPaymentMethod = selectedPaymentMethod.value // Pastikan ini diakses dengan benar

        val validPaymentMethod = when {
            currentPaymentMethod.isNullOrBlank() -> null
            currentPaymentMethod == "null" -> null
            else -> currentPaymentMethod
        }

        return TransactionRequest(
            items = cart.items.map { cartItem ->
                CartItemRequest(
                    menu_id = cartItem.menuId,
                    quantity = cartItem.quantity
                )
            },
            payment_method = validPaymentMethod, // Default ke cash jika null
            customer_id = customerId, // Menggunakan customerId yang diterima sebagai parameter
            nama_transaksi = namaTransaksi ?: "Transaction-${System.currentTimeMillis()}",
            status = "lunas"
        )
    }

    suspend fun prepareTransactionDataPending(namaTransaksi: String? = null, customerId: Int?): TransactionRequest {
        val cart = getCart()
        val currentPaymentMethod = selectedPaymentMethod.value
        val customerIdToSend = if (currentPaymentMethod?.lowercase() == "deposit") {
            customerId // Gunakan customerId yang diterima dari parameter
        } else {
            null // Set customer ID to null for other payment methods
        }

        return TransactionRequest(
            items = cart.items.map { cartItem ->
                CartItemRequest(
                    menu_id = cartItem.menuId,
                    quantity = cartItem.quantity
                )
            },
            payment_method = currentPaymentMethod, // Default ke cash jika null
            customer_id = customerIdToSend,
            nama_transaksi = namaTransaksi ?: "Transaction-${System.currentTimeMillis()}",
            status = "pending"
        )
    }
}