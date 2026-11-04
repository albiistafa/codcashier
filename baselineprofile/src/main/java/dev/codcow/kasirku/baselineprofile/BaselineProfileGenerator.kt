package dev.codcow.kasirku.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Startup, then the Kasir tab with a scroll through the menu grid: the paths a cashier hits every shift.
 * Read-only on purpose (no taps on menu cards), so it never adds to a real cart.
 * Works logged out too: the Kasir tab is then simply not found and only startup is recorded.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "dev.codcow.kasirku", includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.text("Kasir")), 10_000)
        device.findObject(By.text("Kasir"))?.click() ?: return@collect
        device.waitForIdle()
        device.findObject(By.scrollable(true))?.fling(Direction.DOWN)
        device.waitForIdle()
    }
}
