package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
            Image(
                painter = rememberAsyncImagePainter(menu.photo),
                contentDescription = menu.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(102.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Kolom teks (nama, harga, kategori) dengan weight agar delete tetap di kanan
            Column(

            ) {
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically){
                    Column (modifier = Modifier.weight(1f)) {
                        val displayName = if (menu.name.length > 10) {
                            menu.name.substring(0, 10) + "..."
                        } else {
                            menu.name
                        }
                        Text(
                            text = displayName,
                            style = AppTheme.typography.labelSemibold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = formatRupiah(menu.price), style = AppTheme.typography.paragraph1)
                    }
                    Row () {
                        IconButton(onClick = { onEditClick(menu.id) }) {
                            Icon(Icons.Filled.Mode, contentDescription = "Edit", tint = AppTheme.colors.surface)
                        }
                        IconButton(
                            onClick = { onDeleteClick(menu.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
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
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = Modifier
            .border(1.dp, Color(0xFFABCE4D), RoundedCornerShape(10.dp))
            .padding(2.dp)
    ) {
        Text(
            text = text,
            style = AppTheme.typography.paragraph2,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}


