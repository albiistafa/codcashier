package dev.codcow.kasirku.features.splash.ui.screens

import dev.codcow.kasirku.ui.util.rememberReduceMotion
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.codcow.kasirku.features.auth.ui.screens.LoginViewModel
import dev.codcow.kasirku.ui.theme.AppTheme

@Composable
fun Splash(navController: NavController, viewModel: LoginViewModel, context: Context) {
    var animationStarted by remember { mutableStateOf(false) }
    val reduceMotion = rememberReduceMotion()

    // Mulai animasi setelah composable pertama kali muncul
    LaunchedEffect(Unit) {
        animationStarted = true

        if (viewModel.hasValidToken(context)) {
            // Navigasi langsung ke home jika token valid
            navController.navigate("menu") {
                popUpTo("splash") { inclusive = true } // Hapus splash screen dari back stack
            }
        } else {
            // Navigasi ke login jika tidak ada token valid
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true } // Hapus splash screen dari back stack
            }
        }
    }

    // Animasi alpha (fade-in)
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 1000)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.primaryGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Cod Cashier",
                style = AppTheme.typography.heading1Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alpha) // Terapkan animasi alpha
            )
        }
        Text(
            "by Codcow Digital",
            style = AppTheme.typography.paragraph2,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
        )
    }
}
