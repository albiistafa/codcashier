package dev.codcow.kasirku.core.data.model.pengeluaran

data class PengeluaranRequest(
    val nama_barang: String,
    val harga_barang: Int,
    val tanggal_pengeluaran: String
)
