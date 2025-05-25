package dev.codcow.kasirku.core.utils

import dev.codcow.kasirku.core.data.model.transaksi.Data
import java.text.SimpleDateFormat
import java.util.*

object StrukFormatter {

    fun fromTransaction(transaction: Data, warung: dev.codcow.kasirku.core.data.model.warung.Data): String {
        val builder = StringBuilder()

        builder.appendln(centerText(warung.name))
        builder.appendln("==============================")
        builder.appendln("No: ${transaction.id}")
        builder.appendln("Tanggal: ${formatDate(transaction.created_at)}")
        builder.appendln("Payment: ${transaction.payment_method}")
        builder.appendln("------------------------------")

        for (item in transaction.transaction_details) {
            val name = item.menu_name
            val qtyPrice = "${item.quantity} x ${formatCurrency(item.menu_price)}"
            val subtotal = formatCurrency(item.subtotal)

            // Format nama dan subtotal dalam 2 kolom
            builder.appendln("${name.padEnd(16)} ${subtotal.padStart(12)}")
            builder.appendln(qtyPrice)
        }

        builder.appendln("------------------------------")
        builder.appendln("TOTAL".padEnd(16) + formatCurrency(transaction.total_amount).padStart(14))
        builder.appendln("==============================")
        builder.appendln(centerText("Terima Kasih"))
        builder.append("\n\n\n") // Feed line supaya kertas maju

        return builder.toString()
    }

    private fun centerText(text: String, width: Int = 30): String {
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun formatCurrency(amount: String): String {
        return "Rp${amount.replace(".0", "").replace(",", ".")}"
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}
