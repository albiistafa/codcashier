package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

@Composable
fun MenuCardPemesanan(
    menu: DataMenu,
    quantity: Int,
    onIncrement: (DataMenu) -> Unit,
    onDecrement: (DataMenu) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.width(200.dp)
            .height(250.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(menu.photo),
            contentDescription = menu.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(140.dp)
                .width(200.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
                .fillMaxWidth()
        ) {

            Text(
                text = menu.name,
                style = AppTheme.typography.paragraph1Semibold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatRupiah(menu.price),
                style = AppTheme.typography.paragraph2,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { onDecrement(menu) },
                    enabled = quantity > 0,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = "Kurangi",
                        tint = Color(0xFF8EBB2A),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = quantity.toString(),
                    style = AppTheme.typography.paragraph2,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = { onIncrement(menu) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "Tambah",
                        tint = Color(0xFF8EBB2A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewMenuCardPemesanan() {
    val sampleMenu = DataMenu(
        id = 1,
        name = "Soto Ayam bajingan hehe whay boy",
        price = "12000",
        photo = "https://cdn.idntimes.com/content-images/post/20230421/soto-ayam-bening-a8dd1056cd42683fd3ff2f6f90762aa7.jpg",
        category_id = 1,
        sub_category_id = 1,
        created_at = "",
        updated_at = ""
    )

    var quantity by remember { mutableStateOf(1) }

    AppTheme {
        MenuCardPemesanan(
            menu = sampleMenu,
            quantity = quantity,
            onIncrement = { quantity++ },
            onDecrement = {
                if (quantity > 0) quantity--
            }
        )
    }
}
