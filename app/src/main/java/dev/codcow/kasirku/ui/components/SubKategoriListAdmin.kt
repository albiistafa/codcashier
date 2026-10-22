package dev.codcow.kasirku.ui.components

import androidx.compose.runtime.Composable
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori

/** Subkategori row: tap to rename, red button to delete. */
@Composable
fun SubKategoriListItem(
    subkategori: DataSubkategori,
    index: Int,
    count: Int,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    AdminRow(
        index = index, count = count,
        title = subkategori.name,
        onClick = { onEditClick(subkategori.id) },
        leading = { RowIcon(AppIcons.subkategori) },
        trailing = { DeleteButton(subkategori.name) { onDeleteClick(subkategori.id) } }
    )
}
