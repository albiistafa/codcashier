package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

/** Admin menu row: photo, name, price, and "Kategori · Subkategori" as a pill. Tap to edit. */
@Composable
fun MenuCard(
    menu: DataMenu,
    index: Int,
    count: Int,
    categories: Map<Int, String>,
    subCategories: Map<Int, String>,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    val group = listOfNotNull(categories[menu.category_id], menu.sub_category_id?.let { subCategories[it] }).joinToString(" · ")
    AdminRow(
        index = index, count = count,
        title = menu.name,
        onClick = { onEditClick(menu.id) },
        leading = { MenuPhoto(photo = menu.photo, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))) },
        supporting = {
            Text(formatRupiah(menu.price).replace("Rp ", "Rp"), style = AppTheme.typography.paragraph2Semibold)
            if (group.isNotEmpty()) {
                Text(
                    group,
                    style = AppTheme.typography.paragraph2Semibold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        },
        trailing = { DeleteButton(menu.name) { onDeleteClick(menu.id) } }
    )
}
