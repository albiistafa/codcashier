package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

/**
 * Cashier menu card: the whole card is the "+1" target (fast under a queue),
 * the quantity sits in a badge, and "−" only appears once there is something to remove.
 */
@Composable
fun MenuCardPemesanan(
    menu: DataMenu,
    quantity: Int,
    onIncrement: (DataMenu) -> Unit,
    onDecrement: (DataMenu) -> Unit
) {
    val selected = quantity > 0
    Box {
        Card(
            onClick = { onIncrement(menu) },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(2.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = if (selected) "$quantity di pesanan" else "Belum dipesan"
                    onClick(label = "Tambah satu") { onIncrement(menu); true }
                }
        ) {
            MenuPhoto(
                photo = menu.photo,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = menu.name,
                    style = AppTheme.typography.paragraph1Semibold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 40.dp)
                )
                Text(
                    text = formatRupiah(menu.price),
                    style = AppTheme.typography.paragraph2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (selected) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .widthIn(min = 32.dp)
                    .height(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(quantity.toString(), style = AppTheme.typography.paragraph1Semibold, textAlign = TextAlign.Center)
                }
            }
            FilledIconButton(
                onClick = { onDecrement(menu) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurangi ${menu.name}", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMenuCardPemesanan() {
    val sampleMenu = DataMenu(
        id = 1,
        name = "Soto Ayam",
        price = "12000",
        photo = "",
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
            onDecrement = { if (quantity > 0) quantity-- }
        )
    }
}
