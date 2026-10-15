package dev.projecttabs.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class TabOrderingTest {

    @Test
    fun `insert appends new key at the end`() {
        assertEquals(listOf("a", "b", "c"), TabOrdering.insert(listOf("a", "b"), "c", NewTabPlacement.END, anchor = "a"))
    }

    @Test
    fun `insert places new key after the anchor`() {
        assertEquals(listOf("a", "c", "b"), TabOrdering.insert(listOf("a", "b"), "c", NewTabPlacement.AFTER_CURRENT, anchor = "a"))
    }

    @Test
    fun `insert falls back to the end when the anchor is unknown`() {
        assertEquals(listOf("a", "b", "c"), TabOrdering.insert(listOf("a", "b"), "c", NewTabPlacement.AFTER_CURRENT, anchor = "x"))
        assertEquals(listOf("a", "b", "c"), TabOrdering.insert(listOf("a", "b"), "c", NewTabPlacement.AFTER_CURRENT, anchor = null))
    }

    @Test
    fun `insert keeps the position of a known key`() {
        val known = listOf("a", "b")
        assertSame(known, TabOrdering.insert(known, "a", NewTabPlacement.END, anchor = null))
    }

    @Test
    fun `arrange follows the known order and puts unknown keys last`() {
        assertEquals(listOf("b", "a", "x", "y"), TabOrdering.arrange(listOf("closed", "b", "a"), listOf("x", "a", "y", "b")))
    }

    @Test
    fun `move reorders visible keys and keeps closed ones in place`() {
        val known = listOf("a", "closed", "b", "c")
        val visible = listOf("a", "b", "c")
        assertEquals(listOf("c", "closed", "a", "b"), TabOrdering.move(known, visible, "c", 0))
        assertEquals(listOf("b", "closed", "c", "a"), TabOrdering.move(known, visible, "a", 2))
    }

    @Test
    fun `move clamps the target index`() {
        assertEquals(listOf("b", "c", "a"), TabOrdering.move(listOf("a", "b", "c"), listOf("a", "b", "c"), "a", 99))
        assertEquals(listOf("c", "a", "b"), TabOrdering.move(listOf("a", "b", "c"), listOf("a", "b", "c"), "c", -5))
    }

    @Test
    fun `move adds visible keys missing from known`() {
        assertEquals(listOf("b", "a"), TabOrdering.move(listOf("a"), listOf("a", "b"), "b", 0))
    }

    @Test
    fun `move ignores unknown key`() {
        val known = listOf("a", "b")
        assertSame(known, TabOrdering.move(known, listOf("a", "b"), "zzz", 0))
    }

    @Test
    fun `neighbor wraps around in both directions`() {
        val visible = listOf("a", "b", "c")
        assertEquals("b", TabOrdering.neighbor(visible, "a", 1))
        assertEquals("a", TabOrdering.neighbor(visible, "c", 1))
        assertEquals("c", TabOrdering.neighbor(visible, "a", -1))
    }

    @Test
    fun `neighbor is null with a single tab or unknown current`() {
        assertNull(TabOrdering.neighbor(listOf("a"), "a", 1))
        assertNull(TabOrdering.neighbor(listOf("a", "b"), "x", 1))
    }

    @Test
    fun `tab numbers map to indices and 9 means last`() {
        assertEquals(0, TabOrdering.indexForNumber(1, 3))
        assertEquals(2, TabOrdering.indexForNumber(3, 3))
        assertNull(TabOrdering.indexForNumber(4, 3))
        assertEquals(2, TabOrdering.indexForNumber(9, 3))
        assertEquals(10, TabOrdering.indexForNumber(9, 11))
        assertNull(TabOrdering.indexForNumber(1, 0))
        assertNull(TabOrdering.indexForNumber(0, 3))
    }

    @Test
    fun `successor is the right neighbour, or the left one for the last tab`() {
        assertEquals("c", TabOrdering.successorOnClose(listOf("a", "b", "c"), "b"))
        assertEquals("b", TabOrdering.successorOnClose(listOf("a", "b", "c"), "c"))
        assertNull(TabOrdering.successorOnClose(listOf("a"), "a"))
    }

    @Test
    fun `closed stack keeps newest first without duplicates and respects the limit`() {
        assertEquals(listOf("b", "a", "c"), TabOrdering.pushClosed(listOf("a", "b", "c"), "b", 10))
        assertEquals(listOf("d", "a"), TabOrdering.pushClosed(listOf("a", "b", "c"), "d", 2))
    }

    @Test
    fun `prune drops closed keys from the end only`() {
        assertEquals(listOf("open1", "x", "open2"), TabOrdering.prune(listOf("open1", "x", "open2", "y"), setOf("open1", "open2"), 3))
        val small = listOf("a", "b")
        assertSame(small, TabOrdering.prune(small, emptySet(), 5))
    }

}
