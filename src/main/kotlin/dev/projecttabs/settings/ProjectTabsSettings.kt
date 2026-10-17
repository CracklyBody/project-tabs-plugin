package dev.projecttabs.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "ProjectTabsSettings", storages = [Storage("projectTabs.xml")])
class ProjectTabsSettings : PersistentStateComponent<ProjectTabsSettings.State> {

    class State {
        var showTabs: Boolean = true
        var hideWhenSingle: Boolean = false
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): ProjectTabsSettings = service()
    }
}
