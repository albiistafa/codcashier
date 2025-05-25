package dev.codcow.kasirku.features.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun saveBitmapAsPdf(context: Context, bitmap: Bitmap): Uri? {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
    pdfDocument.finishPage(page)

    val filename = "struk_${System.currentTimeMillis()}.pdf"
    var uri: Uri? = null

    try {
        // For Android 10+ (API 29+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Kasirku")
            }

            val resolver = context.contentResolver
            uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let { pdfUri ->
                resolver.openOutputStream(pdfUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                    outputStream.flush()
                } ?: throw IOException("Failed to open output stream")
            } ?: throw IOException("Failed to create document")
        } else {
            // For older Android versions
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val kasirkuDir = File(documentsDir, "Kasirku")
            if (!kasirkuDir.exists()) {
                kasirkuDir.mkdirs()
            }

            val file = File(kasirkuDir, filename)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }

            // Add to media store so it appears in other apps
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DATA, file.absolutePath)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            }
            uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
        }

        return uri
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("PDF_SAVE", "Error saving PDF: ${e.message}")
        return null
    } finally {
        pdfDocument.close()
    }
}
