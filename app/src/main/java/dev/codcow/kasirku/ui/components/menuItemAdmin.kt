package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah
import org.intellij.lang.annotations.JdkConstants

@Composable
fun MenuCard(
    menu: DataMenu,
    categories: Map<Int, String>,
    subCategories: Map<Int, String>,
    onEditClick: (Int) -> Unit ,
    onDeleteClick: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gambar
            MenuPhoto(
                photo = menu.photo,
                modifier = Modifier
                    .size(102.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Kolom teks (nama, harga, kategori) dengan weight agar delete tetap di kanan
            Column(

            ) {
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically){
                    Column (modifier = Modifier.weight(1f)) {
                        Text(
                            text = menu.name,
                            style = AppTheme.typography.labelSemibold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = formatRupiah(menu.price), style = AppTheme.typography.paragraph1)
                    }
                    Row () {
                        IconButton(onClick = { onEditClick(menu.id) }) {
                            Icon(Icons.Filled.Mode, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { onDeleteClick(menu.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()))
                     {
                    categories[menu.category_id]?.let { category ->
                        Chip(category)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    subCategories[menu.sub_category_id]?.let { subCategory ->
                        Chip(subCategory)
                    }
                }
            }


        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .padding(2.dp)
    ) {
        Text(
            text = text,
            style = AppTheme.typography.paragraph2,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
