package dev.projecttabs.service

import com.intellij.util.messages.Topic

fun interface ProjectTabsListener {

    fun tabsChanged()

    companion object {
        val TOPIC: Topic<ProjectTabsListener> = Topic.create("Project Tabs", ProjectTabsListener::class.java)
    }
}
