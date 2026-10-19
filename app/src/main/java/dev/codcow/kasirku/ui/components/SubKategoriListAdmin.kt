package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.ui.theme.AppTheme

@Composable
fun SubKategoriListItem(
    subkategori: DataSubkategori,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nomor & Nama Kategori
            Text(
                text = subkategori.name,
                style = AppTheme.typography.paragraph1Semibold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Tombol Edit
            IconButton(onClick = { onEditClick(subkategori.id)}) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Tombol Delete
            IconButton(onClick = { onDeleteClick(subkategori.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSubKategoriListItem() {
    val sampleKategori = DataSubkategori(
        id = 1,
        name = "MAKANAN",
        category_id = 1
    )
    AppTheme {
        SubKategoriListItem(
            onEditClick = {},
            onDeleteClick = {},
            subkategori = sampleKategori
        )
    }
}
