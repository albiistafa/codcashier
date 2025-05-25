package dev.codcow.kasirku.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme
import kotlin.collections.take
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.codcow.kasirku.core.data.model.kategori.FilterChipItem

@Composable
fun FilterSidebarMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onResetFilter: () -> Unit = {},
    categories: List<FilterChipItem> = emptyList(),
    subCategories: List<FilterChipItem> = emptyList(),
    onApplyFilter: (categoryIds: List<Int>, subCategoryIds: List<Int>) -> Unit = { _, _ -> },
    shadowElevation: Dp = 8.dp
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sidebarWidth = screenWidth * 3 / 4
    val offsetAnimation by animateDpAsState(targetValue = if (isVisible) 0.dp else sidebarWidth, label = "sidebarOffset")

    val selectedCategoryIds = remember { mutableStateOf(setOf<Int>()) }
    val selectedSubCategoryIds = remember { mutableStateOf(setOf<Int>()) }

    // Derived state untuk mengecek apakah ada kategori yang terpilih
    val isCategorySelected by remember {
        derivedStateOf { selectedCategoryIds.value.isNotEmpty() }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .offset(x = offsetAnimation)
                .graphicsLayer(shadowElevation = shadowElevation.value)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Filter", style = AppTheme.typography.labelBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kategori", style = AppTheme.typography.paragraph2)

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    content = {
                        val displayedCategories = categories.take(9)
                        items(displayedCategories.chunked(2)) { rowCategories ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                rowCategories.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategoryIds.value.contains(category.id),
                                        onClick = {
                                            selectedCategoryIds.value = selectedCategoryIds.value.let { set ->
                                                if (set.contains(category.id)) set - category.id else set + category.id
                                            }
                                            // Reset selected sub categories when categories change
                                            if (!selectedCategoryIds.value.contains(category.id)) {
                                                selectedSubCategoryIds.value = emptySet()
                                            }
                                        },
                                        label = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                            ) {
                                                Text(
                                                    category.label,
                                                    style = AppTheme.typography.paragraph2,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF6D8E22),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFECECEC),
                                            labelColor = Color.Black
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color.Transparent,
                                            borderWidth = 0.dp,
                                            enabled = true,
                                            selected = true
                                        )
                                    )
                                }
                                repeat(2 - rowCategories.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tampilkan section Sub Kategori hanya jika ada kategori yang dipilih
                AnimatedVisibility(visible = isCategorySelected) {
                    Column {
                        Text("Sub Kategori", style = AppTheme.typography.paragraph2)
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            content = {
                                val displayedSubCategories = subCategories.take(9)
                                items(displayedSubCategories.chunked(2)) { rowSubCategories ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowSubCategories.forEach { subCategory ->
                                            FilterChip(
                                                selected = selectedSubCategoryIds.value.contains(subCategory.id),
                                                onClick = {
                                                    selectedSubCategoryIds.value =
                                                        selectedSubCategoryIds.value.let { set ->
                                                            if (set.contains(subCategory.id)) set - subCategory.id else set + subCategory.id
                                                        }
                                                },
                                                label = {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp)
                                                    ) {
                                                        Text(
                                                            subCategory.label,
                                                            style = AppTheme.typography.paragraph2,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.align(Alignment.Center)
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF6D8E22),
                                                    selectedLabelColor = Color.White,
                                                    containerColor = Color(0xFFECECEC),
                                                    labelColor = Color.Black
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    borderColor = Color.Transparent,
                                                    borderWidth = 0.dp,
                                                    enabled = true,
                                                    selected = true
                                                )
                                            )
                                        }
                                        repeat(2 - rowSubCategories.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            selectedCategoryIds.value = emptySet()
                            selectedSubCategoryIds.value = emptySet()
                            onResetFilter()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF6D8E22)
                        ),
                        border = BorderStroke(1.dp, color = Color(0xFF6D8E22))
                    ) {
                        Text("Atur Ulang", style = AppTheme.typography.paragraph2)
                    }

                    Button(
                        onClick = {
                            onApplyFilter(
                                selectedCategoryIds.value.toList(),
                                selectedSubCategoryIds.value.toList()
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF6D8E22),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Pakai", style = AppTheme.typography.paragraph2)
                    }
                }
            }
        }
    }
}