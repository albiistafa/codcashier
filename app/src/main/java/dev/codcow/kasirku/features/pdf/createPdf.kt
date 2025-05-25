package dev.codcow.kasirku.features.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun createPdf(context: Context, transactionData: String) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)

    val canvas = page.canvas
    val paint = Paint()
    paint.textSize = 16f

    val lines = transactionData.split("\n")
    var yPosition = 50f

    for (line in lines) {
        canvas.drawText(line, 40f, yPosition, paint)
        yPosition += 25f
    }

    document.finishPage(page)

    try {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Transaksi.pdf")
        document.writeTo(FileOutputStream(file))

    } catch (e: IOException) {
        e.printStackTrace()
    }

    document.close()
}
