package dev.projecttabs.lifecycle

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectCloseListener
import dev.projecttabs.service.ProjectTabsService

internal class TabsProjectCloseListener : ProjectCloseListener {

    override fun projectClosing(project: Project) {
        ProjectTabsService.getInstance().projectClosing(project)
    }

    override fun projectClosed(project: Project) {
        ProjectTabsService.getInstance().projectClosed(project)
    }
}

internal class TabsAppLifecycleListener : AppLifecycleListener {

    override fun appClosing() {
        ProjectTabsService.getInstance().appClosing()
    }
}
