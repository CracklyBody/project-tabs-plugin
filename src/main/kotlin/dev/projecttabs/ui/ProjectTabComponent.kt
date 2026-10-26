package dev.projecttabs.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBUI
import dev.projecttabs.ProjectTabsBundle
import dev.projecttabs.service.ProjectTab
import dev.projecttabs.settings.CloseButtonMode
import dev.projecttabs.settings.ProjectTabsSettings
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.dnd.DragSource
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleRole
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleStateSet
import javax.swing.JPanel
import javax.swing.SwingUtilities

internal class ProjectTabComponent(
    private val strip: ProjectTabsStrip,
    val tab: ProjectTab,
    val isActive: Boolean,
    private val position: Int,
    private val count: Int,
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
    private var dragging = false
    private var accessible: AccessibleContext? = null

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

    private val closeMode: CloseButtonMode
        get() = ProjectTabsSettings.getInstance().state.closeButton

    private val showsClose: Boolean
        get() = !tab.isClosing && when (closeMode) {
            CloseButtonMode.ALWAYS -> true
            CloseButtonMode.HOVER -> isActive || hovered
            CloseButtonMode.NEVER -> false
        }

    private val closeSpace: Int
        get() = if (closeMode == CloseButtonMode.NEVER) 0 else JBUI.scale(CLOSE_GAP) + CLOSE_ICON.iconWidth

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
                dragging || (pressedAt != null && !closeHovered) -> TabsColors.pressedBackground
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
            if (e.isPopupTrigger) {
                strip.showTabMenu(tab, this@ProjectTabComponent, e.x, e.y)
                return
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                pressedAt = e.point
                repaint()
            }
        }

        override fun mouseDragged(e: MouseEvent) {
            val start = pressedAt ?: return
            if (!dragging && start.distance(e.point) >= DragSource.getDragThreshold()) {
                dragging = true
                strip.dragStarted()
            }
            if (dragging) strip.dragMoved(SwingUtilities.convertPoint(this@ProjectTabComponent, e.point, strip).x)
        }

        override fun mouseReleased(e: MouseEvent) {
            if (e.isPopupTrigger) {
                strip.showTabMenu(tab, this@ProjectTabComponent, e.x, e.y)
                return
            }
            val wasPressed = pressedAt != null
            pressedAt = null
            if (dragging) {
                dragging = false
                strip.dragFinished(this@ProjectTabComponent)
                return
            }
            repaint()
            if (!contains(e.point)) return
            when {
                SwingUtilities.isMiddleMouseButton(e) -> strip.closeTab(tab)
                wasPressed && SwingUtilities.isLeftMouseButton(e) ->
                    if (showsClose && closeBounds().contains(e.point)) strip.closeTab(tab) else strip.activateTab(tab)
            }
        }
    }

    override fun getAccessibleContext(): AccessibleContext =
        accessible ?: AccessibleProjectTab().also { accessible = it }

    private inner class AccessibleProjectTab : AccessibleJPanel() {
        override fun getAccessibleRole(): AccessibleRole = AccessibleRole.PAGE_TAB
        override fun getAccessibleName(): String = tab.displayName
        override fun getAccessibleDescription(): String = ProjectTabsBundle.message("accessible.tab.description", position, count)

        override fun getAccessibleStateSet(): AccessibleStateSet =
            super.getAccessibleStateSet().apply { if (isActive) add(AccessibleState.SELECTED) }
    }

    private companion object {
        val CLOSE_ICON = AllIcons.Actions.Close
        const val PADDING_LEFT = 10
        const val PADDING_RIGHT = 6
        const val CLOSE_GAP = 4
        const val MAX_WIDTH = 220
    }
}
