package dev.codcow.kasirku.regression

import dev.codcow.kasirku.core.data.model.transaksi.*
import org.junit.Assert.*
import org.junit.Test

fun pendingTransaction() = Data(
    created_at = "", id = 10, nama_transaksi = "Pelanggan", payment_method = null,
    status = "pending", is_delivered = "no", total_amount = "12000", user_id = 1,
    transaction_details = listOf(TransactionDetailX(7, "Soto", "6000", 2, "12000"))
)

class PaymentUpdateTest {
    @Test fun cashPaymentPreservesItemsAndDelivery() {
        val request = pendingTransaction().toPaymentUpdate("tunai", 99)
        assertEquals("lunas", request.status)
        assertEquals("tunai", request.payment_method)
        assertEquals("no", request.is_delivered)
        assertNull(request.customer_id)
        assertEquals(listOf(MenuItemUpdate(7, 2)), request.items)
    }

    @Test fun digitalPaymentPreservesAlreadyDeliveredStatus() {
        assertEquals("yes", pendingTransaction().copy(is_delivered = "yes")
            .toPaymentUpdate("digital", null).is_delivered)
    }

    @Test fun depositUsesSelectedCustomer() {
        assertEquals(42, pendingTransaction().toPaymentUpdate("deposit", 42).customer_id)
    }

    @Test fun invalidPaymentsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { pendingTransaction().toPaymentUpdate("deposit", null) }
        assertThrows(IllegalArgumentException::class.java) { pendingTransaction().toPaymentUpdate("", null) }
        assertThrows(IllegalArgumentException::class.java) { pendingTransaction().copy(status = "lunas").toPaymentUpdate("tunai", null) }
        assertThrows(IllegalArgumentException::class.java) { pendingTransaction().copy(transaction_details = emptyList()).toPaymentUpdate("tunai", null) }
    }
}
