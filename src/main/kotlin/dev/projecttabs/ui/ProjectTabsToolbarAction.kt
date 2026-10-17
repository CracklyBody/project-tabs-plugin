package dev.projecttabs.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import dev.projecttabs.service.ProjectTabsService
import dev.projecttabs.settings.ProjectTabsSettings
import javax.swing.JComponent

internal class ProjectTabsToolbarAction : DumbAwareAction(), CustomComponentAction {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val settings = ProjectTabsSettings.getInstance().state
        e.presentation.isEnabledAndVisible = e.isFromActionToolbar && project != null && settings.showTabs &&
            !(settings.hideWhenSingle && ProjectTabsService.getInstance().tabCount() < 2)
        e.presentation.putClientProperty(PROJECT_KEY, project)
    }

    override fun actionPerformed(e: AnActionEvent) = Unit

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent = ProjectTabsStrip()

    override fun updateCustomComponent(component: JComponent, presentation: Presentation) {
        (component as? ProjectTabsStrip)?.ownerProject = presentation.getClientProperty(PROJECT_KEY)
    }

    private companion object {
        val PROJECT_KEY: Key<Project> = Key.create("ProjectTabs.ownerProject")
    }
}
