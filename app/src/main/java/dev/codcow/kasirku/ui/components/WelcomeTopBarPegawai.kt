package dev.codcow.kasirku.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.codcow.kasirku.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import dev.codcow.kasirku.features.auth.ui.screens.LoginViewModel
import dev.codcow.kasirku.features.warung.ui.screens.warungViewModel

@Composable
fun WelcomeTopBarPegawai(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: warungViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val warungName by viewModel.warung.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val warungNameChaced by viewModel.cachedWarungName.collectAsState()// Get the loading state
    val name = warungName?.name

    val context = LocalContext.current
    val userRole = loginViewModel.getUserRole(context = context)

    // Jangan fetchWarung di dalam body composable
    LaunchedEffect(Unit) {
        viewModel.fetchWarungIfChanged()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Selamat Datang ${userRole}!",
                style = AppTheme.typography.paragraph1
            )
            if (isLoading) {
                ShimmerAnimation()
            } else {
                Text(
                    text = warungNameChaced ?: "Nama Warung",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.heading3Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Box(modifier = Modifier.size(52.dp)) {
            IconButton(
                onClick = { navController.navigate("home") }
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Pengaturan usaha",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
