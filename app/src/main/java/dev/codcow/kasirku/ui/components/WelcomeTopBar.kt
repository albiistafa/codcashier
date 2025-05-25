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
fun WelcomeTopBar(
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
        Column {
            Text(
                "Selamat Datang ${userRole}!",
                style = AppTheme.typography.paragraph1
            )
            if (isLoading) {
                ShimmerAnimation()
            } else {
                Text(
                    text = if ((warungNameChaced?.length ?: 0) > 14) {
                        warungNameChaced?.substring(0, 14) + "..."
                    } else {
                        warungNameChaced ?: "Nama Warung"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.heading3Bold,
                    color = AppTheme.colors.surface
                )
            }
        }
        Box(
            modifier = Modifier.height(52.dp)
        ) {
            IconButton(
                onClick = { navController.navigate("home") }
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "",
                    modifier = Modifier.size(52.dp)
                )
            }
        }
    }
}

@Composable
fun ShimmerAnimation() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Adjust the target value as needed for the shimmer effect
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000, // Adjust the duration for the shimmer speed
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation.value, 0f),
        end = Offset(translateAnimation.value + 200f, 0f)
    )

    Box(
        modifier = Modifier
            .width(150.dp) // Width of the shimmer box
            .height(30.dp) // Height of the shimmer box
            .clip(RoundedCornerShape(6.dp)) // Clip the shimmer box
            .background(shimmerBrush)
    )
}