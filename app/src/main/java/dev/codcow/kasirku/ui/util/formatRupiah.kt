package dev.codcow.kasirku.ui.util

import java.text.NumberFormat
import java.util.Locale

// Fungsi untuk mengubah angka ke format Rupiah
fun formatRupiah(amount: String): String {
    return try {
        val number = amount.toDouble()  // Pastikan dalam format angka
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(number).replace("Rp", "Rp ")
    } catch (e: NumberFormatException) {
        "Rp 0"
    }
}

fun formatRupiahMines(amount: String): String {
    return try {
        val number = amount.toDouble()  // Pastikan dalam format angka
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        format.format(number).replace("Rp", "-Rp ")
    } catch (e: NumberFormatException) {
        "Rp 0"
    }
}
