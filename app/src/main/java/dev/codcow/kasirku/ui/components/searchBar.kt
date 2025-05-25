package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        placeholder = {
            Text(
                placeholder,
                color = Color.Gray,
                style = AppTheme.typography.paragraph2
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(30.dp),
        textStyle = AppTheme.typography.paragraph2,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFF6D8E22),
            focusedBorderColor = Color(0xFF6D8E22),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color(0xFFEEF5D2),
            unfocusedTextColor = Color.Gray,
            focusedTextColor = Color(0xFF6D8E22)
        )
    )
}

@Preview
@Composable
fun PreviewSearchBar() {
    var query by remember { mutableStateOf("") }

    SearchBar(
        query = query,
        onQueryChange = { query = it },
        placeholder = "tes"
    )
}
