package dev.projecttabs.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.JBUI
import dev.projecttabs.ProjectTabsBundle
import dev.projecttabs.service.ProjectTab
import dev.projecttabs.service.ProjectTabsListener
import dev.projecttabs.service.ProjectTabsService
import dev.projecttabs.switching.ProjectCloser
import dev.projecttabs.switching.ProjectOpener
import dev.projecttabs.switching.ProjectSwitcher
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

internal class ProjectTabsStrip : JPanel(null) {

    var ownerProject: Project? = null
        set(value) {
            if (field === value) return
            field = value
            rebuild()
        }

    private val tabComponents = ArrayList<ProjectTabComponent>()
    private var hiddenTabs: List<ProjectTab> = emptyList()
    private val overflowButton = StripButton(AllIcons.General.ChevronDown, ProjectTabsBundle.message("strip.overflow.tooltip")) {
        showOverflowPopup(it)
    }
    private val newTabButton = StripButton(AllIcons.General.Add, ProjectTabsBundle.message("strip.new.tab.tooltip")) {
        showNewTabPopup(it)
    }

    private var connection: MessageBusConnection? = null
    private var window: Window? = null
    private val windowListener = object : WindowAdapter() {
        override fun windowActivated(e: WindowEvent) {
            ownerProject?.let { ProjectTabsService.getInstance().projectActivated(it) }
            rebuild()
        }
    }

    private var dropIndex: Int? = null

    init {
        isOpaque = false
        border = JBUI.Borders.empty(0, 6)
        add(overflowButton)
        add(newTabButton)
    }

    override fun addNotify() {
        super.addNotify()
        connection = ApplicationManager.getApplication().messageBus.connect().also {
            it.subscribe(ProjectTabsListener.TOPIC, ProjectTabsListener {
                ApplicationManager.getApplication().invokeLater({ rebuild() }, ModalityState.any())
            })
        }
        window = SwingUtilities.getWindowAncestor(this)?.also { it.addWindowListener(windowListener) }
        rebuild()
    }

    override fun removeNotify() {
        connection?.disconnect()
        connection = null
        window?.removeWindowListener(windowListener)
        window = null
        super.removeNotify()
    }

    private fun rebuild() {
        if (connection == null) return
        val tabs = ProjectTabsService.getInstance().tabs()
        tabComponents.forEach { remove(it) }
        tabComponents.clear()
        tabs.forEachIndexed { index, tab ->
            val component = ProjectTabComponent(this, tab, tab.project === ownerProject)
            tabComponents += component
            add(component, index)
        }
        dropIndex = null
        revalidate()
        repaint()
    }

    private val gap: Int get() = JBUI.scale(GAP)

    override fun getPreferredSize(): Dimension {
        val insets = insets
        val tabs = tabComponents.sumOf { it.preferredSize.width } + gap * (tabComponents.size - 1).coerceAtLeast(0)
        return Dimension(insets.left + tabs + gap + newTabButton.preferredSize.width + insets.right, insets.top + JBUI.scale(ROW_HEIGHT) + insets.bottom)
    }

    override fun getMinimumSize(): Dimension {
        val insets = insets
        val tabs = if (tabComponents.isEmpty()) 0 else JBUI.scale(MIN_TAB_WIDTH) + gap + overflowButton.preferredSize.width + gap
        return Dimension(insets.left + tabs + newTabButton.preferredSize.width + insets.right, insets.top + JBUI.scale(ROW_HEIGHT) + insets.bottom)
    }

    override fun doLayout() {
        val insets = insets
        val innerHeight = height - insets.top - insets.bottom
        val rowHeight = minOf(innerHeight, JBUI.scale(ROW_HEIGHT))
        val y = insets.top + (innerHeight - rowHeight) / 2
        val newTabWidth = newTabButton.preferredSize.width
        val overflowWidth = overflowButton.preferredSize.width

        val layout = TabLayout.compute(
            preferred = tabComponents.map { it.preferredSize.width },
            available = width - insets.left - insets.right - newTabWidth - gap,
            minWidth = JBUI.scale(MIN_TAB_WIDTH),
            gap = gap,
            overflowWidth = overflowWidth,
            activeIndex = tabComponents.indexOfFirst { it.isActive },
        )

        tabComponents.forEach { it.setBounds(0, 0, 0, 0) }
        var x = insets.left
        layout.visible.forEachIndexed { slot, index ->
            tabComponents[index].setBounds(x, y, layout.widths[slot], rowHeight)
            x += layout.widths[slot] + gap
        }
        val visible = layout.visible.toSet()
        hiddenTabs = tabComponents.filterIndexed { index, _ -> index !in visible }.map { it.tab }
        if (layout.overflow) {
            overflowButton.setBounds(x, y, overflowWidth, rowHeight)
            x += overflowWidth + gap
        } else {
            overflowButton.setBounds(0, 0, 0, 0)
        }
        newTabButton.setBounds(x, y, newTabWidth, rowHeight)
    }

    override fun paintChildren(g: Graphics) {
        super.paintChildren(g)
        val index = dropIndex ?: return
        val shown = tabComponents.filter { it.width > 0 }
        val anchor = shown.firstOrNull() ?: return
        val target = tabComponents.getOrNull(index)?.takeIf { it.width > 0 }
        val x = if (target != null) target.x - gap / 2 else shown.last().let { it.x + it.width + gap / 2 }
        val lineWidth = JBUI.scale(2)
        g.color = TabsColors.dropIndicator
        g.fillRect(x - lineWidth / 2, anchor.y, lineWidth, anchor.height)
    }

    fun activateTab(tab: ProjectTab) {
        if (tab.project !== ownerProject) ProjectSwitcher.switchTo(tab.project, ownerProject)
    }

    fun closeTab(tab: ProjectTab) {
        ProjectCloser.close(tab.project, activateSuccessor = tab.project === ownerProject)
    }

    fun showTabMenu(tab: ProjectTab, component: JComponent, x: Int, y: Int) {
        ProjectTabMenu.show(tab, ownerProject, component, x, y)
    }

    fun dragStarted() {
        dropIndex = null
        repaint()
    }

    fun dragMoved(x: Int) {
        val shown = tabComponents.filter { it.width > 0 }
        val last = shown.lastOrNull() ?: return
        val before = shown.firstOrNull { x < it.x + it.width / 2 }
        dropIndex = tabComponents.indexOf(before ?: last) + if (before == null) 1 else 0
        repaint()
    }

    fun dragFinished(source: ProjectTabComponent) {
        val target = dropIndex
        dropIndex = null
        val from = tabComponents.indexOf(source)
        if (target == null || from < 0) {
            repaint()
            return
        }
        val toIndex = if (target > from) target - 1 else target
        if (toIndex == from) repaint() else ProjectTabsService.getInstance().move(source.tab.key, toIndex)
    }

    private fun showOverflowPopup(button: JComponent) {
        val group = DefaultActionGroup(hiddenTabs.map { tab -> DumbAwareAction.create(tab.displayName) { activateTab(tab) } })
        JBPopupFactory.getInstance()
            .createActionGroupPopup(null, group, DataManager.getInstance().getDataContext(button), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false)
            .showUnderneathOf(button)
    }

    private fun showNewTabPopup(button: JComponent) {
        val group = DefaultActionGroup()
        val recent = ProjectOpener.recentProjectActions()
        if (recent.isNotEmpty()) {
            group.addSeparator(ProjectTabsBundle.message("popup.new.tab.recent"))
            group.addAll(recent)
            group.addSeparator()
        }
        group.add(DumbAwareAction.create(ProjectTabsBundle.message("popup.new.tab.open"), AllIcons.Actions.MenuOpen) {
            ProjectOpener.chooseAndOpen(ownerProject)
        })
        JBPopupFactory.getInstance()
            .createActionGroupPopup(
                ProjectTabsBundle.message("popup.new.tab.title"),
                group,
                DataManager.getInstance().getDataContext(button),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false,
            )
            .showUnderneathOf(button)
    }

    companion object {
        const val ROW_HEIGHT = 28
        private const val GAP = 2
        private const val MIN_TAB_WIDTH = 72
    }
}
