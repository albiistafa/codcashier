package dev.codcow.kasirku.features.splash.ui.screens

import dev.codcow.kasirku.ui.util.rememberReduceMotion
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.codcow.kasirku.features.auth.ui.screens.LoginViewModel
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.ui.components.StrukMark
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val Ease = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

// The full animation plays once per process; reopening the app while it is still in memory goes straight in.
private var openingPlayed = false

/**
 * The app's only opening animation (the system splash is plain green, see Theme.Kasirku):
 * the struk feeds out of the printer slot, the kunyit total lands, then "Kasirku" and the credit.
 */
@Composable
fun Splash(navController: NavController, viewModel: LoginViewModel, context: Context) {
    val skip = rememberReduceMotion() || openingPlayed
    val paper = remember { Animatable(if (skip) 1f else 0f) }
    val total = remember { Animatable(if (skip) 1f else 0f) }
    val name = remember { Animatable(if (skip) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!skip) {
            listOf(
                launch { delay(150); paper.animateTo(1f, tween(800, easing = Ease)) },
                launch { delay(850); total.animateTo(1f, tween(320, easing = Ease)) },
                launch { delay(950); name.animateTo(1f, tween(450, easing = Ease)) },
            ).joinAll()
            delay(300) // let the name settle before moving on
            openingPlayed = true
        }
        val destination = if (viewModel.hasValidToken(context)) {
            // Pemilik ke Beranda, pegawai langsung ke Kasir
            if (viewModel.getUserRole(context) == "admin") "home" else "menu"
        } else "login"
        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true } // Hapus splash screen dari back stack
        }
    }

    val onHero = AppTheme.colors.onHero
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.hero.first())
            .semantics { contentDescription = "Kasirku sedang dibuka" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StrukMark(paperFraction = paper.value, totalScale = total.value)
            Spacer(Modifier.height(20.dp))
            Text(
                "Kasirku",
                style = AppTheme.typography.heading2Bold.copy(fontSize = 30.sp, letterSpacing = (-0.5).sp),
                color = onHero,
                modifier = Modifier.graphicsLayer { alpha = name.value; translationY = (1f - name.value) * 10.dp.toPx() }
            )
        }
        Text(
            "by Nueu Studios",
            style = AppTheme.typography.paragraph2,
            color = onHero.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .graphicsLayer { alpha = name.value }
        )
    }
}
