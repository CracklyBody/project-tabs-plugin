package dev.projecttabs.ui

import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color

internal object TabsColors {

    val foreground: Color
        get() = JBColor.namedColor("MainToolbar.foreground", JBColor.foreground())

    val inactiveForeground: Color
        get() = ColorUtil.withAlpha(foreground, 0.72)

    val hoverBackground: Color
        get() = JBColor.namedColor("MainToolbar.Dropdown.transparentHoverBackground", ColorUtil.withAlpha(foreground, 0.08))

    val activeBackground: Color
        get() = ColorUtil.withAlpha(foreground, 0.16)

    val pressedBackground: Color
        get() = ColorUtil.withAlpha(foreground, 0.22)

    val dropIndicator: Color
        get() = ColorUtil.withAlpha(foreground, 0.8)

    val arc: Int
        get() = JBUI.CurrentTheme.MainToolbar.Button.hoverArc().get()
}
