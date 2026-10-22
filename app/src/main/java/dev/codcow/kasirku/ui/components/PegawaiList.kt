package dev.codcow.kasirku.ui.components

import androidx.compose.runtime.Composable
import dev.codcow.kasirku.core.data.model.pegawai.Data

/** Pegawai row: name and the phone number they sign in with. The app has no edit for pegawai. */
@Composable
fun PegawaiItem(
    pegawai: Data,
    index: Int,
    count: Int,
    onDeleteClick: (Int) -> Unit
) {
    AdminRow(
        index = index, count = count,
        title = pegawai.name,
        subtitle = "Masuk dengan ${pegawai.phone_number}",
        leading = { InitialsAvatar(pegawai.name) },
        trailing = { DeleteButton(pegawai.name) { onDeleteClick(pegawai.id) } }
    )
}
