package dev.codcow.kasirku.ui.components

import androidx.compose.runtime.Composable
import dev.codcow.kasirku.core.data.model.kategori.DataKategori

/** Kategori row: tap to rename, red button to delete. */
@Composable
fun KategoriListItem(
    kategori: DataKategori,
    index: Int,
    count: Int,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    AdminRow(
        index = index, count = count,
        title = kategori.name,
        onClick = { onEditClick(kategori.id) },
        leading = { RowIcon(AppIcons.kategori) },
        trailing = { DeleteButton(kategori.name) { onDeleteClick(kategori.id) } }
    )
}
