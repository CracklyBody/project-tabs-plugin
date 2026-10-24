package dev.projecttabs.actions

import com.intellij.ide.actions.RevealFileAction
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

internal abstract class GoToProjectTabAction(private val number: Int) : ProjectTabsAction() {
    override fun isEnabled(project: Project) = service.projectAt(number) != null
    override fun perform(project: Project) {
        service.projectAt(number)?.let { if (it !== project) ProjectSwitcher.switchTo(it, project) }
    }
}

internal class GoToProjectTab1Action : GoToProjectTabAction(1)
internal class GoToProjectTab2Action : GoToProjectTabAction(2)
internal class GoToProjectTab3Action : GoToProjectTabAction(3)
internal class GoToProjectTab4Action : GoToProjectTabAction(4)
internal class GoToProjectTab5Action : GoToProjectTabAction(5)
internal class GoToProjectTab6Action : GoToProjectTabAction(6)
internal class GoToProjectTab7Action : GoToProjectTabAction(7)
internal class GoToProjectTab8Action : GoToProjectTabAction(8)
internal class GoToLastProjectTabAction : GoToProjectTabAction(9)

internal class CloseProjectTabAction : ProjectTabsAction() {
    override fun perform(project: Project) = TabCommands.close(project, project)
}

internal class CloseOtherProjectTabsAction : ProjectTabsAction() {
    override fun isEnabled(project: Project) = TabCommands.othersOf(project).isNotEmpty()
    override fun perform(project: Project) = TabCommands.closeOthers(project, project)
}

internal class CloseProjectTabsToRightAction : ProjectTabsAction() {
    override fun isEnabled(project: Project) = TabCommands.rightOf(project).isNotEmpty()
    override fun perform(project: Project) = TabCommands.closeToRight(project, project)
}

internal class ReopenClosedProjectTabAction : ProjectTabsAction() {
    override fun isEnabled(project: Project) = TabCommands.canReopenClosed()
    override fun perform(project: Project) = TabCommands.reopenClosed()
}

internal class CopyProjectPathAction : ProjectTabsAction() {
    override fun isEnabled(project: Project) = project.basePath != null
    override fun perform(project: Project) = TabCommands.copyPath(project)
}

internal class RevealProjectAction : ProjectTabsAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.text = RevealFileAction.getActionName()
    }

    override fun isEnabled(project: Project) = project.basePath != null
    override fun perform(project: Project) = TabCommands.reveal(project)
}
