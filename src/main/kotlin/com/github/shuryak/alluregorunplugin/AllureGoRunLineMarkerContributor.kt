package com.github.shuryak.alluregorunplugin

import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.findAllureSuiteRunner
import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.isAllureTestCandidate
import com.goide.psi.GoMethodDeclaration
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.PsiElement

class AllureGoRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element !is GoMethodDeclaration) return null

        if (!element.isAllureTestCandidate()) {
            return null
        }

        element.findAllureSuiteRunner() ?: return null

        val actions = mutableListOf<AnAction>()
        actions.addAll(ExecutorAction.getActions(0))

        return Info(AllIcons.RunConfigurations.TestState.Run, actions.toTypedArray())
    }
}
