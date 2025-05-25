package dev.codcow.kasirku.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object SnackbarUtil {

    fun showShort(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun showLong(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun showIndefinite(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    fun showWithRetry(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String,
        onRetry: () -> Unit
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Coba Lagi",
                duration = SnackbarDuration.Long
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onRetry()
            }
        }
    }

    fun showWithYesNo(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String,
        onYes: () -> Unit,
        onNo: (() -> Unit)? = null
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Iya",
                duration = SnackbarDuration.Long
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onYes()
            } else {
                onNo?.invoke()
            }
        }
    }
}