package dev.codcow.kasirku.features.beranda.ui.screens

import CustomBottomNavigation
import FoodMenuGrid
import ScrollableCategoryBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.components.SearchBar
import dev.codcow.kasirku.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun beranda() {
    var query by remember { mutableStateOf("") } // State untuk search query

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.onSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Selamat Datang Admin!",
                            style = AppTheme.typography.paragraph1
                        )
                        Text(
                            "Kedai Amira",
                            style = AppTheme.typography.heading3Bold,
                            color = AppTheme.colors.surface
                        )
                    }
                    Box(
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "",
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(17.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Cari menu"
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Kategori",
                        style = AppTheme.typography.paragraph1Bold
                    )
                    Text(
                        "Lihat Semua >",
                        style = AppTheme.typography.paragraph1
                    )
                }

                Spacer(modifier = Modifier.height(11.dp))

                ScrollableCategoryBar()

                Spacer(modifier = Modifier.height(11.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Menu",
                        style = AppTheme.typography.paragraph1Bold
                    )
                    Text(
                        "Lihat Semua >",
                        style = AppTheme.typography.paragraph1
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FoodMenuGrid()
            }
        }
    }
    Column (modifier = Modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
        ){

    }

}


@Preview
@Composable
fun preview() {
    AppTheme { beranda() }
}
