package dev.projecttabs.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabLayoutTest {

    private fun compute(preferred: List<Int>, available: Int, activeIndex: Int = 0) =
        TabLayout.compute(preferred, available, minWidth = 50, gap = 2, overflowWidth = 20, activeIndex = activeIndex)

    @Test
    fun `tabs that fit keep their preferred width`() {
        val result = compute(listOf(100, 120), available = 222)
        assertEquals(listOf(0, 1), result.visible)
        assertEquals(listOf(100, 120), result.widths)
        assertFalse(result.overflow)
    }

    @Test
    fun `wide tabs shrink first and narrow ones keep their width`() {
        val result = compute(listOf(60, 200, 200), available = 304)
        assertEquals(listOf(0, 1, 2), result.visible)
        assertEquals(listOf(60, 120, 120), result.widths)
        assertFalse(result.overflow)
    }

    @Test
    fun `tabs never shrink below the minimum and the rest overflows`() {
        val result = compute(List(5) { 100 }, available = 200)
        assertTrue(result.overflow)
        assertEquals(listOf(0, 1, 2), result.visible)
        assertEquals(3, result.widths.size)
        assertTrue(result.widths.all { it >= 50 })
        assertTrue(result.widths.sum() + 2 * 2 <= 178)
    }

    @Test
    fun `active tab stays visible when it would overflow`() {
        val result = compute(List(5) { 100 }, available = 200, activeIndex = 4)
        assertEquals(listOf(0, 1, 4), result.visible)
    }

    @Test
    fun `at least one tab is shown even without room`() {
        val result = compute(listOf(100, 100), available = 10)
        assertEquals(1, result.visible.size)
        assertEquals(listOf(50), result.widths)
        assertTrue(result.overflow)
    }

    @Test
    fun `no tabs means an empty layout`() {
        val result = compute(emptyList(), available = 100)
        assertTrue(result.visible.isEmpty())
        assertFalse(result.overflow)
    }

    @Test
    fun `fitWidths caps evenly`() {
        assertEquals(listOf(30, 50, 50), TabLayout.fitWidths(listOf(30, 80, 90), total = 130, minWidth = 10))
        assertEquals(listOf(10, 10), TabLayout.fitWidths(listOf(80, 90), total = 5, minWidth = 10))
    }
}
