package dev.codcow.kasirku.ui.util

import retrofit2.HttpException
import java.io.IOException

/**
 * One home for turning a failure into words a cashier can act on:
 * "[what did not happen]. [why, and what to do next]."
 * Repositories wrap failures in their own NetworkException/ApiException/AuthenticationException,
 * so this looks through the whole cause chain instead of trusting `message` ("API error: 404").
 */
fun Throwable.toUserMessage(what: String): String = "$what. ${recoveryFor(this)}"

internal fun recoveryFor(error: Throwable): String {
    val chain = generateSequence(error) { it.cause }.toList()
    val code = chain.filterIsInstance<HttpException>().firstOrNull()?.code()
    return when {
        chain.any { it is IOException || it.javaClass.simpleName == "NetworkException" } ->
            "Koneksi internet terputus atau lambat. Periksa koneksi, lalu coba lagi."
        code == 401 || chain.any { it.javaClass.simpleName == "AuthenticationException" } ->
            "Sesi masuk sudah berakhir. Keluar, lalu masuk lagi."
        code == 403 -> "Akun ini tidak punya akses untuk tindakan ini. Minta pemilik warung mengeceknya."
        code == 404 -> "Datanya tidak ditemukan, mungkin sudah dihapus. Muat ulang daftarnya."
        code != null && code in 400..499 -> "Periksa lagi isiannya, lalu coba lagi."
        code != null -> "Server sedang bermasalah. Coba lagi beberapa saat lagi."
        else -> "Coba lagi. Kalau masih gagal, tutup lalu buka lagi aplikasinya."
    }
}
