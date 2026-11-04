package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

/**
 * A list that failed to load, never a dead end: the illustration, what did not happen as the title,
 * how to recover under it, and one filled Coba lagi.
 */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier, icon: Duo = AppIcons.receipt) {
    val (title, advice) = splitUserMessage(message)
    StatePanel(icon, title, advice, modifier) {
        Button(onClick = onRetry, shape = CircleShape, modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).heightIn(min = 48.dp)) {
            Text("Coba lagi", style = AppTheme.typography.paragraph1Bold)
        }
    }
}

/** Nothing to show yet: what the place is for and, when there is one, the step that fills it. */
@Composable
fun EmptyState(
    icon: Duo,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    action: (@Composable () -> Unit)? = null
) = StatePanel(icon, title, body, modifier, action)

// One layout for every empty and error state: a warm tile with the screen's own icon, like the
// illustrated states in Gojek's design system, drawn with Kasirku's DuoIcons instead of artwork
@Composable
private fun StatePanel(icon: Duo, title: String, body: String?, modifier: Modifier, action: (@Composable () -> Unit)?) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp).semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) { DuoIcon(icon, 44.dp) }
        Spacer(Modifier.height(8.dp))
        Text(title, style = AppTheme.typography.paragraph1Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        body?.let { Text(it, style = AppTheme.typography.paragraph2, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
        action?.let {
            Spacer(Modifier.height(8.dp))
            it()
        }
    }
}

/**
 * Messages from toUserMessage() read "[what did not happen]. [why, and what to do next]."
 * The first sentence becomes the title, the rest the advice; a one-sentence message stays whole.
 */
internal fun splitUserMessage(message: String): Pair<String, String?> {
    val end = message.indexOf(". ")
    if (end <= 0) return message.trim() to null
    val advice = message.substring(end + 2).trim()
    return message.substring(0, end).trim() to advice.ifEmpty { null }
}
