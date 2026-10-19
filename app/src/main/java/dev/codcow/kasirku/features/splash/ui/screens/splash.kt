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
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private val Ease = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/**
 * The app's only opening animation (the system splash is plain green, see Theme.Kasirku):
 * the struk feeds out of the printer slot, the kunyit total lands, then "Kasirku" and the credit.
 */
@Composable
fun Splash(navController: NavController, viewModel: LoginViewModel, context: Context) {
    val reduceMotion = rememberReduceMotion()
    val paper = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val total = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val name = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            listOf(
                launch { delay(150); paper.animateTo(1f, tween(800, easing = Ease)) },
                launch { delay(850); total.animateTo(1f, tween(320, easing = Ease)) },
                launch { delay(950); name.animateTo(1f, tween(450, easing = Ease)) },
            ).joinAll()
            delay(300) // let the name settle before moving on
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
            Box(Modifier.width(132.dp).height(14.dp).background(AppTheme.colors.deep, RoundedCornerShape(7.dp)))
            val teeth = with(LocalDensity.current) { tornBottom(12.dp.toPx(), 5.dp.toPx()) }
            Column(
                Modifier
                    .offset(y = (-7).dp)
                    .width(112.dp)
                    // Reveal from the slot downward, like paper feeding out
                    .graphicsLayer { clip = true; shape = RevealShape(paper.value) }
                    .clip(teeth)
                    .background(onHero)
                    .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                val line = AppTheme.colors.deep.copy(alpha = 0.12f)
                listOf(0.7f, 0.9f, 0.55f).forEach { w ->
                    Box(Modifier.fillMaxWidth(w).height(6.dp).background(line, RoundedCornerShape(3.dp)))
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(line))
                Box(
                    Modifier
                        .align(Alignment.End)
                        .fillMaxWidth(0.62f)
                        .height(8.dp)
                        .graphicsLayer { scaleX = total.value; transformOrigin = TransformOrigin(1f, 0.5f) }
                        .background(AppTheme.colors.money, RoundedCornerShape(4.dp))
                )
            }
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

/** Shows the top [fraction] of the paper. */
private class RevealShape(private val fraction: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Rectangle(Rect(0f, 0f, size.width, size.height * fraction))
}

/** Torn bottom edge of the struk. */
private fun tornBottom(toothWidthPx: Float, toothHeightPx: Float) = GenericShape { size, _ ->
    val teeth = (size.width / toothWidthPx).toInt().coerceAtLeast(1)
    val w = size.width / teeth
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - toothHeightPx)
    repeat(teeth) { i ->
        val x = size.width - i * w
        lineTo(x - w / 2, size.height)
        lineTo(x - w, size.height - toothHeightPx)
    }
    close()
}
