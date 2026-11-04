package dev.codcow.kasirku.core.data.model.transaksi

fun Data.toPaymentUpdate(method: String, customerId: Int?): TransactionUpdateRequest {
    require(status != "lunas") { "Transaksi sudah dibayar" }
    require(method in setOf("tunai", "digital", "deposit")) { "Pilih metode pembayaran" }
    require(method != "deposit" || (customerId != null && customerId > 0)) {
        "Pilih pelanggan deposit"
    }
    require(transaction_details.isNotEmpty() && transaction_details.all { it.menu_id > 0 && it.quantity > 0 }) {
        "Item transaksi tidak valid"
    }
    return TransactionUpdateRequest(
        status = "lunas",
        payment_method = method,
        customer_id = if (method == "deposit") customerId else null,
        is_delivered = is_delivered,
        items = transaction_details.map { MenuItemUpdate(it.menu_id, it.quantity) }
    )
}
