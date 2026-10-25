package dev.projecttabs.onboarding

import com.intellij.ide.GeneralSettings
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import dev.projecttabs.ProjectTabsBundle
import dev.projecttabs.settings.ProjectTabsSettings

internal object OpenInNewWindowNotifier {

    private const val GROUP_ID = "Project Tabs"

    fun notifyOnce(project: Project) {
        val settings = ProjectTabsSettings.getInstance()
        synchronized(settings) {
            if (settings.state.onboardingShown) return
            settings.state.onboardingShown = true
        }
        val general = GeneralSettings.getInstance()
        if (general.confirmOpenNewProject == GeneralSettings.OPEN_PROJECT_NEW_WINDOW) return

        NotificationGroupManager.getInstance().getNotificationGroup(GROUP_ID)
            .createNotification(ProjectTabsBundle.message("notification.new.window.content"), NotificationType.INFORMATION)
            .setTitle(ProjectTabsBundle.message("notification.new.window.title"))
            .addAction(NotificationAction.createSimpleExpiring(ProjectTabsBundle.message("notification.new.window.enable")) {
                general.confirmOpenNewProject = GeneralSettings.OPEN_PROJECT_NEW_WINDOW
            })
            .addAction(NotificationAction.createSimpleExpiring(ProjectTabsBundle.message("notification.new.window.dismiss")) {})
            .notify(project)
    }
}
