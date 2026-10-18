package dev.projecttabs.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import dev.projecttabs.service.ProjectTabsService
import dev.projecttabs.switching.ProjectSwitcher

internal abstract class ProjectTabsAction : DumbAwareAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null && isEnabled(project)
    }

    final override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        perform(project)
    }

    protected open fun isEnabled(project: Project): Boolean = true

    protected abstract fun perform(project: Project)

    protected val service: ProjectTabsService
        get() = ProjectTabsService.getInstance()
}

internal abstract class SwitchProjectTabAction(private val step: Int) : ProjectTabsAction() {
    override fun isEnabled(project: Project) = service.neighbor(project, step) != null
    override fun perform(project: Project) {
        service.neighbor(project, step)?.let { ProjectSwitcher.switchTo(it, project) }
    }
}

internal class NextProjectTabAction : SwitchProjectTabAction(1)

internal class PreviousProjectTabAction : SwitchProjectTabAction(-1)
