package dev.projecttabs.lifecycle

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.projecttabs.service.ProjectTabsService

internal class TabsProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        ProjectTabsService.getInstance().projectOpened(project)
    }
}
