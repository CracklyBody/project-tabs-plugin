package dev.projecttabs.switching

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project

object ProjectSwitcher {

    fun switchTo(target: Project, from: Project?) {
        if (target.isDisposed || target === from) return
        ProjectUtil.focusProjectWindow(target, true)
    }
}
