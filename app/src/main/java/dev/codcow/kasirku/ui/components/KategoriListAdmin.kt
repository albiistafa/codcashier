package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.ui.theme.AppTheme

@Composable
fun KategoriListItem(
    kategori: DataKategori,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                text = kategori.name,
                style = AppTheme.typography.paragraph1Semibold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            // Tombol Edit
            IconButton(onClick = { onEditClick(kategori.id)}) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = AppTheme.colors.surface
                )
            }

            // Tombol Delete
            IconButton(onClick = { onDeleteClick(kategori.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewKategoriListItem() {
    val sampleKategori = DataKategori(
        id = 1,
        name = "MAKANAN"
    )
    AppTheme {
        KategoriListItem(
            kategori = sampleKategori,
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
