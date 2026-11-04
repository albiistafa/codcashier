package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MenuPhoto(photo: String?, modifier: Modifier = Modifier) {
    var unavailable by remember(photo) { mutableStateOf(photo.isNullOrBlank()) }
    BoxWithConstraints(modifier.background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        if (unavailable) {
            // No photo yet: the menu icon on the soft tile, same as the Beranda tiles
            DuoIcon(AppIcons.menu, if (maxWidth >= 90.dp) 36.dp else 24.dp)
        } else {
            AsyncImage(
                // Fade in over the soft tile instead of popping in mid-scroll
                model = ImageRequest.Builder(LocalContext.current).data(photo).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { unavailable = true }
            )
        }
    }
}
