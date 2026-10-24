package dev.projecttabs.actions

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import dev.projecttabs.service.ProjectTabsService
import dev.projecttabs.switching.ProjectCloser
import dev.projecttabs.switching.ProjectOpener
import dev.projecttabs.switching.ProjectSwitcher
import java.awt.datatransfer.StringSelection
import java.nio.file.Path

internal object TabCommands {

    private val service: ProjectTabsService
        get() = ProjectTabsService.getInstance()

    fun close(project: Project, owner: Project?) {
        ProjectCloser.close(project, activateSuccessor = project === owner)
    }

    fun othersOf(project: Project): List<Project> =
        service.tabs().filter { it.project !== project }.map { it.project }

    fun rightOf(project: Project): List<Project> {
        val tabs = service.tabs()
        val index = tabs.indexOfFirst { it.project === project }
        return if (index < 0) emptyList() else tabs.drop(index + 1).map { it.project }
    }

    fun closeOthers(keep: Project, owner: Project?) = closeKeeping(keep, owner, othersOf(keep))

    fun closeToRight(of: Project, owner: Project?) = closeKeeping(of, owner, rightOf(of))

    private fun closeKeeping(keep: Project, owner: Project?, toClose: List<Project>) {
        if (toClose.isEmpty()) return
        if (owner != null && owner !== keep && toClose.any { it === owner }) ProjectSwitcher.switchTo(keep, owner)
        ProjectCloser.closeAll(toClose)
    }

    fun canReopenClosed(): Boolean = service.peekRecentlyClosed() != null

    fun reopenClosed() {
        service.popRecentlyClosed()?.let(ProjectOpener::openInNewWindow)
    }

    fun copyPath(project: Project) {
        project.basePath?.let { CopyPasteManager.getInstance().setContents(StringSelection(it)) }
    }

    fun reveal(project: Project) {
        project.basePath?.let { RevealFileAction.openDirectory(Path.of(it)) }
    }
}
