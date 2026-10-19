package dev.codcow.kasirku.features.berandaAdmin.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionPemasukan
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BerandaState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val masuk: Double = 0.0,
    val keluar: Double = 0.0,
    val pending: List<Transaction> = emptyList(),
    val pendingCount: Int = 0,
    val recent: List<TransactionPemasukan> = emptyList(),
    val salesCount: Int = 0
)

/** Today's numbers for the owner's home. Own state, so it never touches the cashier's TransaksiViewModel. */
@HiltViewModel
class BerandaViewModel @Inject constructor(
    private val repo: TransaksiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BerandaState())
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, failed = false) }
            val today = LocalDate.now().toString()
            val masuk = async { repo.getPemasukanAcrossPage(today, today) }
            val keluar = async { repo.getPengeluaranAcrossPage(today, today) }
            // /transactions/status/{status} 404s on the server; the Transaksi tab's filtered list is the one that works
            val pending = async { repo.getAllTransaksiAcrossPages("", "pending", today, today) }

            val m = masuk.await()
            val k = keluar.await()
            val p = pending.await()
            // The API answers an empty day with a body Gson can't read: that's "nothing yet", not a failure
            val failed = listOf(m, k, p).any { r -> r.exceptionOrNull().let { it != null && it !is JsonSyntaxException } }
            val sales = m.getOrNull()?.transactions.orEmpty()

            _state.value = BerandaState(
                loading = false,
                failed = failed,
                masuk = sales.sumOf { it.total_amount.toDoubleOrNull() ?: 0.0 },
                keluar = k.getOrNull()?.transactions.orEmpty().sumOf { it.total_amount.toDoubleOrNull() ?: 0.0 },
                pending = p.getOrNull()?.transactions.orEmpty(),
                pendingCount = p.getOrNull()?.transactions?.size ?: 0,
                recent = sales.sortedByDescending { it.created_at }.take(4),
                salesCount = sales.size
            )
        }
    }
}
