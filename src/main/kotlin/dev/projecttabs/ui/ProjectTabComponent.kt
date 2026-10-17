package dev.projecttabs.ui

import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBUI
import dev.projecttabs.service.ProjectTab
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

internal class ProjectTabComponent(
    private val strip: ProjectTabsStrip,
    val tab: ProjectTab,
    val isActive: Boolean,
) : JPanel(null) {

    private val label = object : JBLabel(tab.displayName) {
        override fun getForeground(): Color = when {
            tab.isClosing -> ColorUtil.withAlpha(TabsColors.inactiveForeground, 0.5)
            isActive -> TabsColors.foreground
            else -> TabsColors.inactiveForeground
        }
    }

    private var hovered = false
    private var pressedAt: Point? = null

    init {
        isOpaque = false
        add(label)
        toolTipText = buildString {
            append("<html><b>").append(StringUtil.escapeXmlEntities(tab.displayName)).append("</b>")
            tab.path?.let { append("<br>").append(StringUtil.escapeXmlEntities(it)) }
            append("</html>")
        }
        val mouse = TabMouseListener()
        addMouseListener(mouse)
    }

    override fun getPreferredSize(): Dimension {
        val width = JBUI.scale(PADDING_LEFT) + label.preferredSize.width + JBUI.scale(PADDING_RIGHT)
        return Dimension(width.coerceAtMost(JBUI.scale(MAX_WIDTH)), JBUI.scale(ProjectTabsStrip.ROW_HEIGHT))
    }

    override fun doLayout() {
        val labelHeight = label.preferredSize.height
        val labelWidth = (width - JBUI.scale(PADDING_LEFT) - JBUI.scale(PADDING_RIGHT)).coerceAtLeast(0)
        label.setBounds(JBUI.scale(PADDING_LEFT), (height - labelHeight) / 2, labelWidth, labelHeight)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            GraphicsUtil.setupAAPainting(g2)
            val background = when {
                pressedAt != null -> TabsColors.pressedBackground
                isActive -> TabsColors.activeBackground
                hovered -> TabsColors.hoverBackground
                else -> null
            }
            if (background != null) {
                g2.color = background
                g2.fillRoundRect(0, 0, width, height, TabsColors.arc, TabsColors.arc)
            }
        } finally {
            g2.dispose()
        }
    }

    private inner class TabMouseListener : MouseAdapter() {

        override fun mouseEntered(e: MouseEvent) {
            hovered = true
            repaint()
        }

        override fun mouseExited(e: MouseEvent) {
            hovered = false
            repaint()
        }

        override fun mousePressed(e: MouseEvent) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                pressedAt = e.point
                repaint()
            }
        }

        override fun mouseReleased(e: MouseEvent) {
            val wasPressed = pressedAt != null
            pressedAt = null
            repaint()
            if (!contains(e.point)) return
            if (wasPressed && SwingUtilities.isLeftMouseButton(e)) strip.activateTab(tab)
        }
    }

    private companion object {
        const val PADDING_LEFT = 10
        const val PADDING_RIGHT = 6
        const val MAX_WIDTH = 220
    }
}
