package dev.codcow.kasirku

import dev.codcow.kasirku.ui.util.toUserMessage
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UserMessageTest {
    private class ApiException(cause: Throwable) : Exception("API error", cause)
    private class NetworkException(cause: Throwable) : Exception("net", cause)
    private class AuthenticationException : Exception("No authentication token")

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun mapsFailuresToRecoverableIndonesianMessages() {
        val what = "Kategori belum terhapus"
        assertEquals("$what. Koneksi internet terputus atau lambat. Periksa koneksi, lalu coba lagi.",
            NetworkException(IOException()).toUserMessage(what))
        assertEquals("$what. Datanya tidak ditemukan, mungkin sudah dihapus. Muat ulang daftarnya.",
            ApiException(http(404)).toUserMessage(what))
        assertEquals("$what. Sesi masuk sudah berakhir. Keluar, lalu masuk lagi.",
            ApiException(http(401)).toUserMessage(what))
        assertEquals("$what. Sesi masuk sudah berakhir. Keluar, lalu masuk lagi.",
            AuthenticationException().toUserMessage(what))
        assertEquals("$what. Periksa lagi isiannya, lalu coba lagi.",
            ApiException(http(422)).toUserMessage(what))
        assertEquals("$what. Server sedang bermasalah. Coba lagi beberapa saat lagi.",
            ApiException(http(500)).toUserMessage(what))
        assertEquals("$what. Coba lagi. Kalau masih gagal, tutup lalu buka lagi aplikasinya.",
            IllegalStateException("boom").toUserMessage(what))
    }
}
