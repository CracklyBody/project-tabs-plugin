package dev.projecttabs.switching

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import dev.projecttabs.service.ProjectTabsService

object ProjectCloser {

    private const val CLOSE_PROJECT_ACTION_ID = "CloseProject"
    private const val PLACE = "ProjectTabs"

    fun close(project: Project, activateSuccessor: Boolean) {
        if (project.isDisposed) return
        if (activateSuccessor) {
            val service = ProjectTabsService.getInstance()
            service.successorOnClose(project)?.let { service.activateAfterClose(project, it, ProjectSwitcher.geometryOf(project)) }
        }
        ApplicationManager.getApplication().invokeLater { closeNow(project) }
    }

    fun closeAll(projects: List<Project>) {
        for (project in projects) {
            ApplicationManager.getApplication().invokeLater { closeNow(project) }
        }
    }

    private fun closeNow(project: Project) {
        if (project.isDisposed) return
        val action = ActionManager.getInstance().getAction(CLOSE_PROJECT_ACTION_ID)
        val frame = WindowManager.getInstance().getFrame(project)
        if (action == null || frame == null) {
            ProjectManager.getInstance().closeAndDispose(project)
            return
        }
        ActionManager.getInstance()
            .tryToExecute(action, null, frame.rootPane, PLACE, true)
            .doWhenRejected(Runnable { if (!project.isDisposed) ProjectManager.getInstance().closeAndDispose(project) })
    }
}
