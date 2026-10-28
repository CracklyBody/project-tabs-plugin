package dev.projecttabs.switching

import java.awt.Dimension
import java.awt.Rectangle

internal object WindowPlacement {

    private const val MAX_SCREEN_SHARE = 0.75

    fun normalBoundsOn(size: Dimension, screen: Rectangle): Rectangle {
        val width = minOf(size.width, (screen.width * MAX_SCREEN_SHARE).toInt())
        val height = minOf(size.height, (screen.height * MAX_SCREEN_SHARE).toInt())
        return Rectangle(screen.x + (screen.width - width) / 2, screen.y + (screen.height - height) / 2, width, height)
    }
}
