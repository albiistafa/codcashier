package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
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
                CategoryChip("Semua", selected = selectedCategoryId == null) { onSelect(null, null) }
            }
            items(categories, key = { it.id }) { c ->
                CategoryChip(c.name, selected = c.id == selectedCategoryId) { onSelect(c.id, null) }
            }
        }
        if (selectedCategoryId != null && subs.isNotEmpty()) {
            val categoryName = categories.find { it.id == selectedCategoryId }?.name.orEmpty()
            // Sub-kategori read as tabs under the chips, so the two levels never look alike
            Box {
                HorizontalDivider(Modifier.align(Alignment.BottomStart), color = MaterialTheme.colorScheme.outlineVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    item(key = "all-sub") {
                        SubTab("Semua ${categoryName.lowercase()}", selected = selectedSubCategoryId == null) {
                            onSelect(selectedCategoryId, null)
                        }
                    }
                    items(subs, key = { it.id }) { s ->
                        SubTab(s.name, selected = s.id == selectedSubCategoryId) { onSelect(selectedCategoryId, s.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val line = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .heightIn(min = 44.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .drawBehind { if (selected) drawRect(line, Offset(0f, size.height - 2.5.dp.toPx()), Size(size.width, 2.5.dp.toPx())) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = if (selected) AppTheme.typography.paragraph2Semibold else AppTheme.typography.paragraph2,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Kategori chip: daun tua when selected. */
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = AppTheme.typography.paragraph2Semibold) },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppTheme.colors.deep,
            selectedLabelColor = Color.White,
            containerColor = colors.surface,
            labelColor = colors.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, selected = selected,
            borderColor = colors.outlineVariant, selectedBorderColor = AppTheme.colors.deep
        ),
        modifier = Modifier.height(40.dp)
    )
}
