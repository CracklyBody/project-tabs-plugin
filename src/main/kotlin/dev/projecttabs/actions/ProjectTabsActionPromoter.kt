package dev.projecttabs.actions

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext

internal class ProjectTabsActionPromoter : ActionPromoter {

    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
        actions.filter { it is NextProjectTabAction || it is PreviousProjectTabAction }
}
