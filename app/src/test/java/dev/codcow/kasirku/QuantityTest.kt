package dev.codcow.kasirku

import dev.codcow.kasirku.ui.util.parseQuantity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuantityTest {
    @Test fun wholeNumbersAreQuantities() {
        assertEquals(12, parseQuantity("12"))
        assertEquals(7, parseQuantity(" 007 "))
        assertEquals(999, parseQuantity("999"))
    }

    @Test fun zeroRemovesTheItem() = assertEquals(0, parseQuantity("0"))

    @Test fun anythingElseKeepsTheOldAmount() {
        listOf("", "  ", "-1", "1.5", "1,5", "12a", "1000", "1 2").forEach { assertNull(it, parseQuantity(it)) }
    }
}
