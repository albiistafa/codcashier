import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dev.codcow.kasirku.ui.theme.AppTheme
import java.util.UUID
import android.util.Log
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.appcompat.app.AppCompatActivity

suspend fun captureComposableToBitmap(
    context: Context,
    content: @Composable () -> Unit
): Bitmap = suspendCoroutine { continuation ->
    try {
        Log.d("PDF_CAPTURE", "Starting composable capture")

        // Use the app's activity as context if available
        val activityContext = context.findActivity() ?: context

        // Create a frame layout to host our ComposeView
        val frameLayout = FrameLayout(activityContext)

        // Create the ComposeView within the frame
        val composeView = ComposeView(activityContext).apply {
            setContent {
                AppTheme {
                    content()
                }
            }
        }

        // Add the ComposeView to the FrameLayout
        frameLayout.addView(composeView)

        // Measure and layout
        val measureWidth = View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)
        val measureHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        frameLayout.measure(measureWidth, measureHeight)
        frameLayout.layout(0, 0, frameLayout.measuredWidth, frameLayout.measuredHeight)

        // Wait for composition to be ready
        composeView.post {
            try {
                val bitmap = Bitmap.createBitmap(
                    frameLayout.width,
                    frameLayout.height,
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(bitmap)
                frameLayout.draw(canvas)

                Log.d("PDF_CAPTURE", "Bitmap created successfully: ${bitmap.width}x${bitmap.height}")
                continuation.resume(bitmap)
            } catch (e: Exception) {
                Log.e("PDF_CAPTURE", "Error creating bitmap: ${e.message}", e)
                // Create a fallback bitmap with error message
                val fallbackBitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
                continuation.resume(fallbackBitmap)
            }
        }
    } catch (e: Exception) {
        Log.e("PDF_CAPTURE", "Error in capture process: ${e.message}", e)
        // Create a fallback bitmap
        val fallbackBitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
        continuation.resume(fallbackBitmap)
    }
}

// Extension function to find the activity from a context
fun Context.findActivity(): AppCompatActivity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}