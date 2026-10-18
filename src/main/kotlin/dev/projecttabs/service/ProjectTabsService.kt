package dev.projecttabs.service

import com.intellij.ide.ActivityTracker
import com.intellij.ide.lightEdit.LightEdit
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import dev.projecttabs.switching.ProjectSwitcher
import dev.projecttabs.switching.WindowGeometry
import java.util.concurrent.ConcurrentHashMap

data class ProjectTab(
    val key: String,
    val project: Project,
    val displayName: String,
    val path: String?,
    val isClosing: Boolean,
)

@Service(Service.Level.APP)
@State(name = "ProjectTabsOrder", storages = [Storage(value = "projectTabsOrder.xml", roamingType = RoamingType.DISABLED)])
class ProjectTabsService : PersistentStateComponent<ProjectTabsService.State> {

    class State {
        var order: MutableList<String> = mutableListOf()
    }

    private var myState = State()
    private val closingKeys = ConcurrentHashMap.newKeySet<String>()
    private val successors = ConcurrentHashMap<String, Pair<Project, WindowGeometry?>>()

    @Volatile
    private var activeKey: String? = null

    @Volatile
    private var previousActiveKey: String? = null

    @Synchronized
    override fun getState(): State = myState

    @Synchronized
    override fun loadState(state: State) {
        myState = state
    }

    @Synchronized
    fun tabs(): List<ProjectTab> {
        val projects = ProjectManager.getInstance().openProjects.filter { isTabProject(it) }
        val byKey = LinkedHashMap<String, Project>()
        for (project in projects) byKey.putIfAbsent(keyOf(project), project)

        var known: List<String> = myState.order
        for (key in byKey.keys) {
            if (key !in known) {
                val anchor = if (activeKey == key) previousActiveKey else activeKey
                known = TabOrdering.insert(known, key, NewTabPlacement.END, anchor)
            }
        }
        if (known !== myState.order) {
            myState.order = TabOrdering.prune(known, byKey.keys, MAX_KNOWN).toMutableList()
        }

        val ordered = TabOrdering.arrange(myState.order, byKey.keys.toList())
        val names = TabOrdering.displayNames(ordered.map { TabName(it, byKey.getValue(it).name, byKey.getValue(it).basePath) })
        return ordered.map { key ->
            val project = byKey.getValue(key)
            ProjectTab(key, project, names.getValue(key), project.basePath, key in closingKeys)
        }
    }

    fun tabCount(): Int = ProjectManager.getInstance().openProjects.count { isTabProject(it) }

    fun successorOnClose(project: Project): Project? {
        val tabs = tabs()
        val key = TabOrdering.successorOnClose(tabs.map { it.key }, keyOf(project)) ?: return null
        return tabs.first { it.key == key }.project
    }

    fun activateAfterClose(project: Project, successor: Project, geometry: WindowGeometry?) {
        successors[keyOf(project)] = successor to geometry
    }

    fun projectActivated(project: Project) {
        val key = keyOf(project)
        if (key == activeKey) return
        previousActiveKey = activeKey
        activeKey = key
    }

    fun projectOpened(project: Project) {
        if (isTabProject(project)) fireTabsChanged()
    }

    fun projectClosing(project: Project) {
        if (!isTabProject(project)) return
        closingKeys += keyOf(project)
        fireTabsChanged()
    }

    fun projectClosed(project: Project) {
        if (project.isDefault) return
        val key = keyOf(project)
        closingKeys -= key
        val successor = successors.remove(key)
        if (isTabProject(project)) {
            synchronized(this) {
                myState.order = myState.order.filter { it != key }.toMutableList()
            }
        }
        fireTabsChanged()
        if (successor != null) {
            ApplicationManager.getApplication().invokeLater {
                val (target, geometry) = successor
                if (!target.isDisposed) ProjectSwitcher.switchTo(target, geometry)
            }
        }
    }

    fun fireTabsChanged() {
        ApplicationManager.getApplication().messageBus.syncPublisher(ProjectTabsListener.TOPIC).tabsChanged()
        ActivityTracker.getInstance().inc()
    }

    companion object {
        private const val MAX_KNOWN = 50

        fun getInstance(): ProjectTabsService = service()

        fun keyOf(project: Project): String = project.basePath ?: project.locationHash

        fun isTabProject(project: Project): Boolean =
            !project.isDefault && !project.isDisposed && !LightEdit.owns(project)
    }
}
