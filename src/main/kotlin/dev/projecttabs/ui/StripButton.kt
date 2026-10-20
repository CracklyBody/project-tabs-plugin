package dev.projecttabs.ui

import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.SwingUtilities

internal class StripButton(
    private val icon: Icon,
    private val accessibleName: String,
    private val onClick: (StripButton) -> Unit,
) : JComponent() {

    private var hovered = false
    private var pressed = false
    private var accessible: AccessibleContext? = null

    init {
        toolTipText = accessibleName
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) = setHovered(true)
            override fun mouseExited(e: MouseEvent) = setHovered(false)

            override fun mousePressed(e: MouseEvent) {
                if (!SwingUtilities.isLeftMouseButton(e)) return
                pressed = true
                repaint()
            }

            override fun mouseReleased(e: MouseEvent) {
                if (!pressed) return
                pressed = false
                repaint()
                if (contains(e.point)) onClick(this@StripButton)
            }
        })
    }

    private fun setHovered(value: Boolean) {
        hovered = value
        repaint()
    }

    override fun getPreferredSize(): Dimension = Dimension(icon.iconWidth + JBUI.scale(10), icon.iconHeight + JBUI.scale(10))

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            GraphicsUtil.setupAAPainting(g2)
            val background = when {
                pressed -> TabsColors.pressedBackground
                hovered -> TabsColors.hoverBackground
                else -> null
            }
            if (background != null) {
                g2.color = background
                g2.fillRoundRect(0, 0, width, height, TabsColors.arc, TabsColors.arc)
            }
            icon.paintIcon(this, g2, (width - icon.iconWidth) / 2, (height - icon.iconHeight) / 2)
        } finally {
            g2.dispose()
        }
    }

    override fun getAccessibleContext(): AccessibleContext =
        accessible ?: AccessibleStripButton().also { accessible = it }

    private inner class AccessibleStripButton : AccessibleJComponent() {
        override fun getAccessibleRole(): AccessibleRole = AccessibleRole.PUSH_BUTTON
        override fun getAccessibleName(): String = accessibleName
    }
}
