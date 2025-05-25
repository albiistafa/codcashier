import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import dev.codcow.kasirku.R
import dev.codcow.kasirku.ui.theme.AppTheme


@Composable
fun FoodMenuGrid() {
    val foods = listOf(
        FoodItem("Soto Ayam", "Rp 12.000", R.drawable.soto_ayam),
        FoodItem("Soto Ayam", "Rp 12.000", R.drawable.soto_ayam),
        FoodItem("Soto Ayam", "Rp 12.000", R.drawable.soto_ayam),
        FoodItem("Soto Ayam", "Rp 12.000", R.drawable.soto_ayam)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(),
        contentPadding = PaddingValues(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(foods) { food ->
            FoodCard(food)
        }
    }
}

@Composable
fun FoodCard(food: FoodItem) {
    var quantity by remember { mutableStateOf(1) }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box (modifier = Modifier.fillMaxSize()){
                Image(
                    painter = painterResource(id = food.imageRes),
                    contentDescription = food.name,
                    modifier = Modifier
                        .size(149.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column (

                ){
                    Text(
                        text = food.name,
                        style = AppTheme.typography.paragraph2Medium
                    )

                    Text(
                        text = food.price,
                        style = AppTheme.typography.paragraph2Medium
                    )
                }

                IconButton(onClick = { if (quantity > 0) quantity-- }) {
                    Icon(Icons.Default.Remove, contentDescription = "Kurangi", tint = Color(0xFF5F7C1E))
                }

                Text(text = quantity.toString(), style = AppTheme.typography.paragraph1Medium)

                IconButton(onClick = { quantity++ }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color(0xFF5F7C1E))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFoodMenuGrid() {
    AppTheme {
        FoodMenuGrid()
    }
}

data class FoodItem(val name: String, val price: String, @DrawableRes val imageRes: Int)
