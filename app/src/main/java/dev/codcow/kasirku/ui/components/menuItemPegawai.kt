package dev.codcow.kasirku.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import dev.codcow.kasirku.ui.util.QUANTITY_MAX_DIGITS
import dev.codcow.kasirku.ui.util.parseQuantity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.util.formatRupiah

/**
 * Cashier menu card: the whole card is the "+1" target (fast under a queue).
 * Once ordered, the bottom turns into a − qty + stepper and the quantity also sits in a badge.
 */
@Composable
fun MenuCardPemesanan(
    menu: DataMenu,
    quantity: Int,
    onIncrement: (DataMenu) -> Unit,
    onDecrement: (DataMenu) -> Unit,
    onSetQuantity: (DataMenu, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = quantity > 0
    val price = formatRupiah(menu.price).replace("Rp ", "Rp")
    Box(modifier) {
        Card(
            onClick = { onIncrement(menu) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = if (selected) "$quantity di pesanan" else "Belum dipesan"
                    onClick(label = "Tambah satu") { onIncrement(menu); true }
                }
        ) {
            MenuPhoto(photo = menu.photo, modifier = Modifier.height(108.dp).fillMaxWidth())
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = menu.name,
                    style = AppTheme.typography.paragraph2Semibold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 36.dp)
                )
                if (selected) {
                    Text(price, style = AppTheme.typography.paragraph1Bold)
                    Stepper(menu, quantity, onIncrement, onDecrement, onSetQuantity)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(price, style = AppTheme.typography.paragraph1Bold, modifier = Modifier.weight(1f))
                        RoundButton(Icons.Default.Add, "Tambah ${menu.name}", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, 44) {
                            onIncrement(menu)
                        }
                    }
                }
            }
        }
        if (selected) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).widthIn(min = 26.dp).height(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 7.dp)) {
                    RollingCount(quantity, AppTheme.typography.paragraph2.copy(fontWeight = FontWeight.ExtraBold))
                }
            }
        }
    }
}

@Composable
private fun Stepper(
    menu: DataMenu,
    quantity: Int,
    onIncrement: (DataMenu) -> Unit,
    onDecrement: (DataMenu) -> Unit,
    onSetQuantity: (DataMenu, Int) -> Unit
) {
    var typing by remember { mutableStateOf(false) }
    if (typing) QuantityDialog(menu.name, quantity, onSave = { onSetQuantity(menu, it); typing = false }, onDismiss = { typing = false })
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundButton(Icons.Default.Remove, "Kurangi ${menu.name}", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onPrimaryContainer, 40) {
            onDecrement(menu)
        }
        // The count is also a field: 20 es teh is one tap and two digits, not twenty taps on +
        // The whole 44dp-tall middle is the target; the white pill is only how it looks
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(CircleShape)
                .clickable(onClickLabel = "Ketik jumlah ${menu.name}") { typing = true },
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier.padding(horizontal = 6.dp).fillMaxWidth().height(32.dp).background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                RollingCount(quantity, AppTheme.typography.paragraph1Bold, MaterialTheme.colorScheme.onSurface)
            }
        }
        RoundButton(Icons.Default.Add, "Tambah ${menu.name}", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, 40) {
            onIncrement(menu)
        }
    }
}

/** Type the amount instead of tapping + many times. 0 takes the menu off the order. */
@Composable
private fun QuantityDialog(name: String, quantity: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    val current = quantity.toString()
    // Starts fully selected, so typing replaces the old amount instead of appending to it
    var field by remember { mutableStateOf(TextFieldValue(current, TextRange(0, current.length))) }
    val focus = remember { FocusRequester() }
    val parsed = parseQuantity(field.text)
    fun save() { parsed?.let(onSave) }
    LaunchedEffect(Unit) { focus.requestFocus() }
    ConfirmDialog(
        title = "Jumlah $name",
        message = "Ketik jumlah pesanan. Isi 0 untuk menghapusnya dari pesanan.",
        confirmLabel = if (parsed == 0) "Hapus dari pesanan" else "Simpan",
        onConfirm = ::save,
        onDismiss = onDismiss,
        destructive = parsed == 0
    ) {
        OutlinedTextField(
            value = field,
            onValueChange = { if (it.text.length <= QUANTITY_MAX_DIGITS && it.text.all(Char::isDigit)) field = it },
            label = { Text("Jumlah") },
            singleLine = true,
            isError = parsed == null,
            supportingText = if (parsed == null) {{ Text("Isi angka, misalnya 2") }} else null,
            textStyle = AppTheme.typography.heading5Bold,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { save() }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().focusRequester(focus)
        )
    }
}

/** Quantity that rolls up on +1 and down on −1, so each tap visibly lands (odometer feel). */
@Composable
private fun RollingCount(quantity: Int, style: TextStyle, color: Color = Color.Unspecified) {
    AnimatedContent(
        targetState = quantity,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                (slideOutVertically { if (up) -it else it } + fadeOut())
        },
        label = "quantity"
    ) { n ->
        Text(n.toString(), style = style, color = color, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    size: Int,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = container, contentColor = content),
        modifier = Modifier.size(size.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
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
            onDecrement = { if (quantity > 0) quantity-- },
            onSetQuantity = { _, n -> quantity = n }
        )
    }
}
