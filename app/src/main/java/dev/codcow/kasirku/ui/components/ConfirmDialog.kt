package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

/**
 * The one confirmation dialog. Name the thing and the consequence in [message];
 * [confirmLabel] says the action ("Hapus kategori"), never "Ya". Destructive confirms are red.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val accent = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                Modifier.size(48.dp).background(if (destructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (destructive) Icons.Outlined.Delete else Icons.Outlined.CheckCircle, contentDescription = null, tint = accent)
            }
        },
        title = { Text(title, style = AppTheme.typography.heading5Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message, style = AppTheme.typography.paragraph1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content?.invoke()
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = if (destructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary)
            ) { Text(confirmLabel, style = AppTheme.typography.paragraph2Semibold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", style = AppTheme.typography.paragraph2Semibold) }
        }
    )
}
