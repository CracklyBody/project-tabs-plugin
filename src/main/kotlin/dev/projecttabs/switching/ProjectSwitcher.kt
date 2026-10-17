package dev.projecttabs.switching

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import dev.projecttabs.settings.ProjectTabsSettings
import java.awt.Frame
import java.awt.Point
import java.awt.Rectangle

data class WindowGeometry(val bounds: Rectangle, val maximized: Boolean, val fullScreen: Boolean)

object ProjectSwitcher {

    fun geometryOf(project: Project?): WindowGeometry? {
        if (project == null || project.isDisposed) return null
        val windowManager = WindowManager.getInstance()
        val frame = windowManager.getFrame(project) ?: return null
        return WindowGeometry(
            bounds = frame.bounds,
            maximized = isMaximized(frame),
            fullScreen = windowManager.getIdeFrame(project)?.isInFullScreen == true,
        )
    }

    fun switchTo(target: Project, from: Project?) {
        switchTo(target, if (from != null && from !== target) geometryOf(from) else null)
    }

    fun switchTo(target: Project, geometry: WindowGeometry?) {
        if (target.isDisposed) return
        if (geometry != null && ProjectTabsSettings.getInstance().state.snapWindows) snap(target, geometry)
        ProjectUtil.focusProjectWindow(target, true)
    }

    private fun snap(target: Project, source: WindowGeometry) {
        if (source.fullScreen) return
        val windowManager = WindowManager.getInstance()
        if (windowManager.getIdeFrame(target)?.isInFullScreen == true) return
        val frame = windowManager.getFrame(target) ?: return

        if ((frame.extendedState and Frame.ICONIFIED) != 0) {
            frame.extendedState = frame.extendedState and Frame.ICONIFIED.inv()
        }
        val targetMaximized = isMaximized(frame)
        if (source.maximized) {
            if (!targetMaximized || !source.bounds.contains(centerOf(frame.bounds))) {
                frame.extendedState = Frame.NORMAL
                frame.bounds = source.bounds
                frame.extendedState = Frame.MAXIMIZED_BOTH
            }
        } else {
            if (targetMaximized) frame.extendedState = frame.extendedState and Frame.MAXIMIZED_BOTH.inv()
            if (frame.bounds != source.bounds) frame.bounds = source.bounds
        }
    }

    private fun isMaximized(frame: Frame): Boolean = (frame.extendedState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH

    private fun centerOf(bounds: Rectangle) = Point(bounds.centerX.toInt(), bounds.centerY.toInt())
}
