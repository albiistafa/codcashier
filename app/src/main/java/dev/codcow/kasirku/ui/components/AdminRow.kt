package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

/**
 * One row of an admin list. Rows [index] of [count] share one white card (rounded only at the ends,
 * hairlines between), so a list reads as a single group. Tapping the row opens it ([onClick]).
 */
@Composable
fun AdminRow(
    index: Int,
    count: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable ColumnScope.() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val r = 24.dp
    val shape = RoundedCornerShape(
        topStart = if (index == 0) r else 0.dp, topEnd = if (index == 0) r else 0.dp,
        bottomStart = if (index == count - 1) r else 0.dp, bottomEnd = if (index == count - 1) r else 0.dp
    )
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClickLabel = "Ubah", onClick = onClick) else Modifier)
    ) {
        Row(
            Modifier.heightIn(min = 68.dp).padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leading?.invoke()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTheme.typography.paragraph1Semibold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) Text(subtitle, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                supporting?.invoke(this)
            }
            trailing()
            if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (index < count - 1) HorizontalDivider(Modifier.padding(start = 16.dp, end = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Soft square holding a two-tone icon, the leading slot of most admin rows. */
@Composable
fun RowIcon(icon: Duo) {
    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
        DuoIcon(icon, 22.dp)
    }
}

/** Initials avatar for people (pegawai, pelanggan deposit). */
@Composable
fun InitialsAvatar(name: String) {
    val initials = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "?" }
    Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Text(initials, style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/** Red delete button; [name] goes into the label so TalkBack says what gets deleted. */
@Composable
fun DeleteButton(name: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Outlined.Delete, contentDescription = "Hapus $name", tint = MaterialTheme.colorScheme.error)
    }
}

/** "+ Tambah …" button floating over admin lists. */
@Composable
fun AddFab(label: String, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text(label, style = AppTheme.typography.paragraph1Bold) },
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

/** One-line hint above a list, saying what a tap does. */
@Composable
fun ListHint(text: String) {
    Text(text, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
}
