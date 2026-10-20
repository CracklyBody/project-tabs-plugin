package dev.projecttabs.switching

import com.intellij.icons.AllIcons
import com.intellij.ide.ProjectGroupActionGroup
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.ReopenProjectAction
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import dev.projecttabs.ProjectTabsBundle
import dev.projecttabs.service.ProjectTabsService
import java.nio.file.Path

object ProjectOpener {

    private const val MAX_RECENT = 15

    fun openInNewWindow(path: String) {
        ApplicationManager.getApplication().invokeLater {
            ProjectUtil.openOrImport(Path.of(path), null, true)
        }
    }

    fun chooseAndOpen(project: Project?) {
        val descriptor = FileChooserDescriptorFactory.singleFileOrDir()
            .withTitle(ProjectTabsBundle.message("dialog.open.project.title"))
        FileChooser.chooseFile(descriptor, project, null) { file -> openInNewWindow(file.path) }
    }

    fun recentProjectActions(): List<AnAction> {
        val open = ProjectTabsService.getInstance().tabs().mapNotNull { it.path }.toSet()
        val recent = RecentProjectsManager.getInstance().getRecentProjectsActions(false)
            .flatMap { if (it is ProjectGroupActionGroup) it.childActionsOrStubs.asList() else listOf(it) }
            .filterIsInstance<ReopenProjectAction>()
            .filter { it.projectPath !in open && !it.isRemoved }
            .take(MAX_RECENT)
        val duplicateNames = recent.groupBy { it.projectNameToDisplay }.filterValues { it.size > 1 }.keys
        return recent.map { action ->
            val name = action.projectNameToDisplay
            val text = if (name in duplicateNames) "$name — ${action.projectPathToDisplay}" else name
            DumbAwareAction.create(text, action.projectIcon ?: AllIcons.Nodes.Folder) { openInNewWindow(action.projectPath) }
        }
    }
}
