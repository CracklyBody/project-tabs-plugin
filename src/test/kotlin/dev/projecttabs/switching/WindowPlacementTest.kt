package dev.projecttabs.switching

import org.junit.Assert.assertEquals
import org.junit.Test
import java.awt.Dimension
import java.awt.Rectangle

class WindowPlacementTest {

    @Test
    fun `a window that fits is centered on the screen`() {
        assertEquals(Rectangle(500, 300, 1000, 600), WindowPlacement.normalBoundsOn(Dimension(1000, 600), Rectangle(0, 0, 2000, 1200)))
    }

    @Test
    fun `a window on a screen left of the main one is centered there`() {
        assertEquals(Rectangle(-1500, 300, 1000, 600), WindowPlacement.normalBoundsOn(Dimension(1000, 600), Rectangle(-2000, 0, 2000, 1200)))
    }

    @Test
    fun `a window as large as the screen shrinks to three quarters of it`() {
        assertEquals(Rectangle(316, 190, 1296, 837), WindowPlacement.normalBoundsOn(Dimension(3008, 1569), Rectangle(100, 50, 1728, 1117)))
    }
}
