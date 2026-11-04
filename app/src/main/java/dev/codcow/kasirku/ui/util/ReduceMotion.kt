package dev.codcow.kasirku.ui.util

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * True when the user turned on Settings › Accessibility › "Remove animations"
 * (animator duration scale 0). Looping and decorative motion should render static then.
 */
// ponytail: read once per composition; a live toggle applies the next time the screen opens
@Composable
fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
