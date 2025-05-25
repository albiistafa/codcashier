package dev.codcow.kasirku.features.beranda.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarMenu(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onFilterClick: () -> Unit
) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .height(48.dp)
                ,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = AppTheme.colors.surface,
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

        IconButton(
            onClick =  onFilterClick ,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mi_filter),
                contentDescription = "Filter Icon",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    

}

@Preview
@Composable
fun PreviewSearchBar() {
    var query by remember { mutableStateOf("") }

    SearchBarMenu(
        query = query,
        onQueryChange = { query = it },
        placeholder = "",
        onFilterClick = {},
    )
}
