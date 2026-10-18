package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.ui.theme.AppTheme

/**
 * Kategori row, then — once one is picked — its sub-kategori row.
 * Two short rows instead of one long one, so the cashier never scrolls past every sub-kategori.
 */
@Composable
fun CategoryChips(
    categories: List<DataKategori>,
    subCategories: List<DataSubkategori>,
    selectedCategoryId: Int?,
    selectedSubCategoryId: Int?,
    onSelect: (categoryId: Int?, subCategoryId: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subs = subCategories.filter { it.category_id == selectedCategoryId }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
            item(key = "all") {
                CategoryChip("Semua", selected = selectedCategoryId == null, strong = true) { onSelect(null, null) }
            }
            items(categories, key = { it.id }) { c ->
                CategoryChip(c.name, selected = c.id == selectedCategoryId, strong = true) { onSelect(c.id, null) }
            }
        }
        if (selectedCategoryId != null && subs.isNotEmpty()) {
            val categoryName = categories.find { it.id == selectedCategoryId }?.name.orEmpty()
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item(key = "all-sub") {
                    CategoryChip("Semua ${categoryName.lowercase()}", selected = selectedSubCategoryId == null, strong = false) {
                        onSelect(selectedCategoryId, null)
                    }
                }
                items(subs, key = { it.id }) { s ->
                    CategoryChip(s.name, selected = s.id == selectedSubCategoryId, strong = false) {
                        onSelect(selectedCategoryId, s.id)
                    }
                }
            }
        }
    }
}

/** [strong] = kategori (dark when selected); otherwise a quieter tonal sub-kategori chip. */
@Composable
private fun CategoryChip(label: String, selected: Boolean, strong: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = if (strong) AppTheme.typography.paragraph2Medium else AppTheme.typography.paragraph2) },
        colors = if (strong) FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.onPrimaryContainer,
            selectedLabelColor = colors.primaryContainer,
            containerColor = colors.surface
        ) else FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
            labelColor = colors.onSurfaceVariant
        ),
        border = if (strong) FilterChipDefaults.filterChipBorder(
            enabled = true, selected = selected,
            borderColor = colors.outlineVariant, selectedBorderColor = colors.onPrimaryContainer
        ) else null
    )
}
