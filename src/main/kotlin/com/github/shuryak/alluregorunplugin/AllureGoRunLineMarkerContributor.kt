package com.github.shuryak.alluregorunplugin

import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.isAllureFrameworkProviderT
import com.goide.psi.GoMethodDeclaration
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.PsiElement

class AllureGoRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element !is GoMethodDeclaration) return null

        val functionName = element.name ?: return null
        if (!functionName.startsWith("Test")) return null

        val parameters = element.signature?.parameters?.parameterDeclarationList ?: return null
        if (parameters.size != 1) return null

        val parameter = parameters[0]
        if (!parameter.isAllureFrameworkProviderT()) return null

//        val action = RunAllureGoTestAction()
        val actions = mutableListOf<AnAction>()
        actions.addAll(ExecutorAction.getActions(0))

        return Info(AllIcons.RunConfigurations.TestState.Run, actions.toTypedArray())
    }
}
