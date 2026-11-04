package dev.codcow.kasirku.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.codcow.kasirku.R

@Composable
fun MenuLoadState(hasError: Boolean, onRetry: () -> Unit) {
    if (hasError) {
        ErrorState(stringResource(R.string.menu_load_failed), onRetry, icon = AppIcons.menu)
    } else {
        val (title, hint) = splitUserMessage(stringResource(R.string.menu_empty))
        EmptyState(AppIcons.menu, title, body = hint)
    }
}
