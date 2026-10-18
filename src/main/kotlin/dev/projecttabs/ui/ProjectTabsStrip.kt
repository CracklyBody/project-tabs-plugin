package dev.projecttabs.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.JBUI
import dev.projecttabs.service.ProjectTab
import dev.projecttabs.service.ProjectTabsListener
import dev.projecttabs.service.ProjectTabsService
import dev.projecttabs.switching.ProjectCloser
import dev.projecttabs.switching.ProjectSwitcher
import java.awt.Dimension
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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

    private var connection: MessageBusConnection? = null
    private var window: Window? = null
    private val windowListener = object : WindowAdapter() {
        override fun windowActivated(e: WindowEvent) {
            ownerProject?.let { ProjectTabsService.getInstance().projectActivated(it) }
            rebuild()
        }
    }

    init {
        isOpaque = false
        border = JBUI.Borders.empty(0, 6)
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
        revalidate()
        repaint()
    }

    private val gap: Int get() = JBUI.scale(GAP)

    override fun doLayout() {
        val insets = insets
        val innerHeight = height - insets.top - insets.bottom
        val rowHeight = minOf(innerHeight, JBUI.scale(ROW_HEIGHT))
        val y = insets.top + (innerHeight - rowHeight) / 2
        var x = insets.left
        for (component in tabComponents) {
            val width = component.preferredSize.width
            component.setBounds(x, y, width, rowHeight)
            x += width + gap
        }
    }

    override fun getPreferredSize(): Dimension {
        val insets = insets
        val tabs = tabComponents.sumOf { it.preferredSize.width } + gap * (tabComponents.size - 1).coerceAtLeast(0)
        return Dimension(insets.left + tabs + insets.right, insets.top + JBUI.scale(ROW_HEIGHT) + insets.bottom)
    }

    fun activateTab(tab: ProjectTab) {
        if (tab.project !== ownerProject) ProjectSwitcher.switchTo(tab.project, ownerProject)
    }

    fun closeTab(tab: ProjectTab) {
        ProjectCloser.close(tab.project, activateSuccessor = tab.project === ownerProject)
    }

    companion object {
        const val ROW_HEIGHT = 28
        private const val GAP = 2
    }
}
