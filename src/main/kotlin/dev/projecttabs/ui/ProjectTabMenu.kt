package dev.projecttabs.ui

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import dev.projecttabs.ProjectTabsBundle
import dev.projecttabs.actions.TabCommands
import dev.projecttabs.service.ProjectTab
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

internal object ProjectTabMenu {

    private const val PLACE = "ProjectTabs.TabPopup"

    fun show(tab: ProjectTab, owner: Project?, component: JComponent, x: Int, y: Int) {
        val project = tab.project
        val group = DefaultActionGroup().apply {
            add(item(message("action.ProjectTabs.Close.text")) { TabCommands.close(project, owner) })
            add(item(message("action.ProjectTabs.CloseOthers.text"), TabCommands.othersOf(project).isNotEmpty()) {
                TabCommands.closeOthers(project, owner)
            })
            add(item(message("action.ProjectTabs.CloseToRight.text"), TabCommands.rightOf(project).isNotEmpty()) {
                TabCommands.closeToRight(project, owner)
            })
            addSeparator()
            add(item(message("action.ProjectTabs.ReopenClosed.text"), TabCommands.canReopenClosed()) { TabCommands.reopenClosed() })
            addSeparator()
            add(item(message("action.ProjectTabs.CopyPath.text"), tab.path != null) { TabCommands.copyPath(project) })
            add(item(RevealFileAction.getActionName(), tab.path != null) { TabCommands.reveal(project) })
        }
        ActionManager.getInstance().createActionPopupMenu(PLACE, group).component.show(component, x, y)
    }

    private fun message(key: String): String = ProjectTabsBundle.message(key)

    private fun item(@Nls text: String, enabled: Boolean = true, run: () -> Unit): AnAction = object : DumbAwareAction(text) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = enabled
        }

        override fun actionPerformed(e: AnActionEvent) = run()
    }
}
