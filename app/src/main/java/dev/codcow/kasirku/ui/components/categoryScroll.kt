import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

@Composable
fun ScrollableCategoryBar() {
    val categories = listOf("Makanan", "Minuman", "Camilan", "Lauk", "Dessert", "Buah", "Sayur")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        categories.forEachIndexed { index, category ->
            CategoryItem(
                text = category,
                isSelected = category == selectedCategory,
                onClick = { selectedCategory = category },
                isFirstItem = index == 0 // Menentukan apakah ini item pertama
            )
        }
    }
}

@Composable
fun CategoryItem(text: String, isSelected: Boolean, onClick: () -> Unit, isFirstItem: Boolean) {
    Box(
        modifier = Modifier
            .padding(
                start = if (isFirstItem) 0.dp else 14.dp, // Menghilangkan padding kiri di item pertama
                end = 4.dp
            )
            .background(
                color = if (isSelected) Color(0xFF5F7C1E) else Color(0xFFEFF4C2),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = AppTheme.typography.paragraph2,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScrollableCategoryBar() {
    AppTheme{
        ScrollableCategoryBar()
    }

}
