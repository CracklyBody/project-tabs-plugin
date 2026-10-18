package dev.projecttabs.ui

import com.intellij.icons.AllIcons
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
import java.awt.Rectangle
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
    private var closeHovered = false
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
        addMouseMotionListener(mouse)
    }

    private val showsClose: Boolean
        get() = !tab.isClosing && (isActive || hovered)

    private val closeSpace: Int
        get() = JBUI.scale(CLOSE_GAP) + CLOSE_ICON.iconWidth

    private fun closeBounds(): Rectangle {
        val icon = CLOSE_ICON
        return Rectangle(width - JBUI.scale(PADDING_RIGHT) - icon.iconWidth, (height - icon.iconHeight) / 2, icon.iconWidth, icon.iconHeight)
    }

    override fun getPreferredSize(): Dimension {
        val width = JBUI.scale(PADDING_LEFT) + label.preferredSize.width + closeSpace + JBUI.scale(PADDING_RIGHT)
        return Dimension(width.coerceAtMost(JBUI.scale(MAX_WIDTH)), JBUI.scale(ProjectTabsStrip.ROW_HEIGHT))
    }

    override fun doLayout() {
        val labelHeight = label.preferredSize.height
        val labelWidth = (width - JBUI.scale(PADDING_LEFT) - JBUI.scale(PADDING_RIGHT) - closeSpace).coerceAtLeast(0)
        label.setBounds(JBUI.scale(PADDING_LEFT), (height - labelHeight) / 2, labelWidth, labelHeight)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            GraphicsUtil.setupAAPainting(g2)
            val background = when {
                pressedAt != null && !closeHovered -> TabsColors.pressedBackground
                isActive -> TabsColors.activeBackground
                hovered -> TabsColors.hoverBackground
                else -> null
            }
            if (background != null) {
                g2.color = background
                g2.fillRoundRect(0, 0, width, height, TabsColors.arc, TabsColors.arc)
            }
            if (showsClose) {
                val bounds = closeBounds()
                (if (closeHovered) AllIcons.Actions.CloseHovered else CLOSE_ICON).paintIcon(this, g2, bounds.x, bounds.y)
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
            closeHovered = false
            repaint()
        }

        override fun mouseMoved(e: MouseEvent) {
            val overClose = showsClose && closeBounds().contains(e.point)
            if (overClose != closeHovered) {
                closeHovered = overClose
                repaint()
            }
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
            when {
                SwingUtilities.isMiddleMouseButton(e) -> strip.closeTab(tab)
                wasPressed && SwingUtilities.isLeftMouseButton(e) ->
                    if (showsClose && closeBounds().contains(e.point)) strip.closeTab(tab) else strip.activateTab(tab)
            }
        }
    }

    private companion object {
        val CLOSE_ICON = AllIcons.Actions.Close
        const val PADDING_LEFT = 10
        const val PADDING_RIGHT = 6
        const val CLOSE_GAP = 4
        const val MAX_WIDTH = 220
    }
}
