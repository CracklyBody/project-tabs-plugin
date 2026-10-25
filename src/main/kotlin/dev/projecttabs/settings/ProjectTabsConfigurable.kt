package dev.projecttabs.settings

import com.intellij.ide.GeneralSettings
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import dev.projecttabs.ProjectTabsBundle.message
import dev.projecttabs.service.NewTabPlacement

internal class ProjectTabsConfigurable : BoundConfigurable(message("settings.display.name")) {

    override fun createPanel(): DialogPanel {
        val state = ProjectTabsSettings.getInstance().state
        val general = GeneralSettings.getInstance()
        return panel {
            row {
                checkBox(message("settings.show.tabs")).bindSelected(state::showTabs)
            }
            row {
                checkBox(message("settings.hide.single")).bindSelected(state::hideWhenSingle)
            }
            row {
                checkBox(message("settings.snap.windows"))
                    .bindSelected(state::snapWindows)
                    .comment(message("settings.snap.windows.comment"))
            }
            row {
                checkBox(message("settings.open.new.window"))
                    .bindSelected(
                        { general.confirmOpenNewProject == GeneralSettings.OPEN_PROJECT_NEW_WINDOW },
                        { selected ->
                            if (selected) {
                                general.confirmOpenNewProject = GeneralSettings.OPEN_PROJECT_NEW_WINDOW
                            } else if (general.confirmOpenNewProject == GeneralSettings.OPEN_PROJECT_NEW_WINDOW) {
                                general.confirmOpenNewProject = GeneralSettings.OPEN_PROJECT_ASK
                            }
                        },
                    )
                    .comment(message("settings.open.new.window.comment"))
            }
            buttonsGroup(message("settings.close.button")) {
                row { radioButton(message("settings.close.button.hover"), CloseButtonMode.HOVER) }
                row { radioButton(message("settings.close.button.always"), CloseButtonMode.ALWAYS) }
                row { radioButton(message("settings.close.button.never"), CloseButtonMode.NEVER) }
            }.bind(state::closeButton)
            buttonsGroup(message("settings.new.tab.placement")) {
                row { radioButton(message("settings.new.tab.placement.end"), NewTabPlacement.END) }
                row { radioButton(message("settings.new.tab.placement.after.current"), NewTabPlacement.AFTER_CURRENT) }
            }.bind(state::newTabPlacement)
        }
    }

    override fun apply() {
        super.apply()
        ProjectTabsSettings.getInstance().settingsChanged()
    }
}
