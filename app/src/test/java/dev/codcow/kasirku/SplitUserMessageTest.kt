package dev.codcow.kasirku

import dev.codcow.kasirku.ui.components.splitUserMessage
import dev.codcow.kasirku.ui.util.toUserMessage
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class SplitUserMessageTest {
    @Test fun userMessageSplitsIntoTitleAndAdvice() {
        val message = IOException("timeout").toUserMessage("Daftar pegawai belum bisa dimuat")
        assertEquals(
            "Daftar pegawai belum bisa dimuat" to "Koneksi internet terputus atau lambat. Periksa koneksi, lalu coba lagi.",
            splitUserMessage(message)
        )
    }

    @Test fun oneSentenceStaysWhole() {
        assertEquals("Tidak ada data transaksi" to null, splitUserMessage("Tidak ada data transaksi"))
        assertEquals("Periksa koneksi internet Anda" to null, splitUserMessage("Periksa koneksi internet Anda"))
    }

    @Test fun trailingPeriodGivesNoEmptyAdvice() {
        assertEquals("Gagal." to null, splitUserMessage("Gagal."))
        assertEquals("Gagal" to null, splitUserMessage("Gagal. "))
    }
}
