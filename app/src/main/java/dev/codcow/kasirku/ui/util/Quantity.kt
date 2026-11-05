package dev.codcow.kasirku.ui.util

/** Most digits a typed quantity may have. A typo guard (999), not a stock limit: the API sends no stock. */
const val QUANTITY_MAX_DIGITS = 3

/**
 * What the cashier typed in "Ubah jumlah", as a quantity: 0 means take it off the order.
 * Null for anything that is not a whole number, so the dialog keeps the old amount instead of guessing.
 */
fun parseQuantity(text: String): Int? {
    val digits = text.trim()
    if (digits.isEmpty() || digits.length > QUANTITY_MAX_DIGITS || !digits.all { it.isDigit() }) return null
    return digits.toInt()
}
