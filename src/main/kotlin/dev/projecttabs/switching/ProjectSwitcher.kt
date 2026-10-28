package dev.projecttabs.switching

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import dev.projecttabs.settings.ProjectTabsSettings
import java.awt.Frame
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.Timer

data class WindowGeometry(val bounds: Rectangle, val maximized: Boolean, val fullScreen: Boolean, val screen: Rectangle)

object ProjectSwitcher {

    fun geometryOf(project: Project?): WindowGeometry? {
        if (project == null || project.isDisposed) return null
        val windowManager = WindowManager.getInstance()
        val frame = windowManager.getFrame(project) ?: return null
        return WindowGeometry(
            bounds = frame.bounds,
            maximized = isMaximized(frame),
            fullScreen = windowManager.getIdeFrame(project)?.isInFullScreen == true,
            screen = frame.graphicsConfiguration.bounds,
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
        val onSourceScreen = frame.graphicsConfiguration.bounds == source.screen
        if (source.maximized && isMaximized(frame) && onSourceScreen) return
        if (isMaximized(frame)) frame.extendedState = frame.extendedState and Frame.MAXIMIZED_BOTH.inv()
        when {
            !source.maximized -> moveTo(frame, source.bounds)
            !onSourceScreen -> moveTo(frame, WindowPlacement.normalBoundsOn(frame.size, source.screen)) { maximize(frame) }
            else -> maximize(frame)
        }
    }

    private fun moveTo(frame: JFrame, bounds: Rectangle, then: () -> Unit = {}) {
        frame.bounds = bounds
        val deadline = System.currentTimeMillis() + SETTLE_TIMEOUT_MS
        var last = frame.bounds
        var restingTicks = 0
        val timer = Timer(SETTLE_POLL_MS, null)
        timer.addActionListener {
            val current = frame.bounds
            restingTicks = if (current == last) restingTicks + 1 else 0
            last = current
            when {
                !frame.isDisplayable -> timer.stop()
                (current == bounds && restingTicks >= RESTING_TICKS) || System.currentTimeMillis() > deadline -> {
                    timer.stop()
                    then()
                }
                current != bounds && restingTicks >= RESTING_TICKS -> frame.bounds = bounds
            }
        }
        timer.start()
    }

    private fun maximize(frame: Frame) {
        frame.extendedState = frame.extendedState or Frame.MAXIMIZED_BOTH
    }

    private fun isMaximized(frame: Frame): Boolean = (frame.extendedState and Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH

    private const val SETTLE_POLL_MS = 50
    private const val RESTING_TICKS = 3
    private const val SETTLE_TIMEOUT_MS = 1500L
}
