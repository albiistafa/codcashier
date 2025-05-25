package dev.codcow.kasirku.features.splash.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay

@Composable
fun Splash(navController: NavController, viewModel: LoginViewModel, context: Context) {
    var animationStarted by remember { mutableStateOf(false) }

    // Mulai animasi setelah composable pertama kali muncul
    LaunchedEffect(Unit) {
        delay(300) // Delay sebelum animasi dimulai
        animationStarted = true
        delay(1000) // Tunggu hingga animasi selesai (1 detik)

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
        animationSpec = tween(durationMillis = 1000) // Durasi animasi 1 detik
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
                color = AppTheme.colors.surface,
                modifier = Modifier.alpha(alpha) // Terapkan animasi alpha
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 710.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                "by Codcow Digital",
                style = AppTheme.typography.paragraph2,
                textAlign = TextAlign.Center
            )
        }
    }
}
