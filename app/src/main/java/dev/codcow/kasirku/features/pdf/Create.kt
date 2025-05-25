package dev.codcow.kasirku.features.pdf
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import dev.codcow.kasirku.core.data.model.transaksi.Data
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import dev.codcow.kasirku.core.data.model.warung.Data as WarungData

fun createReceiptBitmap(context: Context, transaction: Data, warung: WarungData?): Bitmap {

    val width = 600
    val height = 800 // We'll adjust this based on content
    val padding = 40
    val lineSpacing = 30
    val dividerPadding = 20

    // Create a bitmap and canvas
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    // Setup paints
    val titlePaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val normalPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 16f
        isAntiAlias = true
    }

    val boldPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val dividerPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
    }

    // Start drawing
    var y = padding

    // Draw warung name
    val warungName = warung?.name ?: "Toko Kasirku"
    val warungNameWidth = titlePaint.measureText(warungName)
    canvas.drawText(warungName, (width - warungNameWidth) / 2, y.toFloat(), titlePaint)
    y += lineSpacing + 10

    // Draw transaction info
    canvas.drawText("No: ${transaction.id ?: "-"}", padding.toFloat(), y.toFloat(), normalPaint)
    y += lineSpacing

    canvas.drawText("Tanggal: ${formatDate(transaction.created_at)}" , padding.toFloat(), y.toFloat(), normalPaint)
    y += lineSpacing

    canvas.drawText("Payment: ${transaction.payment_method ?: "-"}", padding.toFloat(), y.toFloat(), normalPaint)
    y += lineSpacing

    // Draw divider
    canvas.drawLine(padding.toFloat(), (y + dividerPadding).toFloat(),
        (width - padding).toFloat(), (y + dividerPadding).toFloat(), dividerPaint)
    y += dividerPadding * 2

    // Draw items
    transaction.transaction_details?.forEach { item ->
        val text = "${item.menu_name ?: "-"} x${item.quantity ?: 0} = Rp${item.subtotal ?: 0}"
        canvas.drawText(text, padding.toFloat(), y.toFloat(), normalPaint)
        y += lineSpacing
    }

    // Draw divider
    canvas.drawLine(padding.toFloat(), (y + dividerPadding).toFloat(),
        (width - padding).toFloat(), (y + dividerPadding).toFloat(), dividerPaint)
    y += dividerPadding * 2

    // Draw total
    val totalText = formatCurrency(transaction.total_amount)
    canvas.drawText(totalText, padding.toFloat(), y.toFloat(), boldPaint)
    y += lineSpacing * 2

    // Draw thank you
    val thankYouText = "Terima Kasih"
    val thankYouWidth = normalPaint.measureText(thankYouText)
    canvas.drawText(thankYouText, (width - thankYouWidth) / 2, y.toFloat(), normalPaint)

    // Return the bitmap with adjusted height
    val adjustedHeight = y + padding
    val adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, Math.min(adjustedHeight, height))

    return adjustedBitmap

}

private fun formatCurrency(amount: String): String {
    val normalized = amount.replace(",", ".") // pastikan format titik desimal
    val number = BigDecimal(normalized)
    val formatted = number.stripTrailingZeros().toPlainString()
    return "Rp$formatted"
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