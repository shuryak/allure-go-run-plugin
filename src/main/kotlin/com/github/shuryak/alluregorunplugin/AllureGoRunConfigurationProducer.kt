package com.github.shuryak.alluregorunplugin

import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.findAllureSuiteRunner
import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.isAllureTestCandidate
import com.goide.execution.GoBuildingRunConfiguration
import com.goide.psi.GoMethodDeclaration
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class AllureGoRunConfigurationProducer : LazyRunConfigurationProducer<AllureGoTestRunConfiguration>() {
    override fun getConfigurationFactory(): ConfigurationFactory {
        return AllureGoTestConfigurationType.instance.configurationFactories[0]
    }

    override fun setupConfigurationFromContext(
        configuration: AllureGoTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val method = context.psiLocation as? GoMethodDeclaration ?: return false

        if (!method.isAllureTestCandidate()) {
            return false
        }

        val suiteRunner = method.findAllureSuiteRunner() ?: return false

        configuration.name = "allure-go ${method.name}"
        configuration.kind = GoBuildingRunConfiguration.Kind.PACKAGE
        configuration.`package` = suiteRunner.containingFile.getImportPath(true).toString()
        configuration.pattern = "^\\Q${suiteRunner.name}\\E$"
        configuration.workingDirectory = suiteRunner.containingFile.virtualFile?.parent?.path.toString()
        configuration.params = "-test.count=1 -allure-go.m ^\\Q${method.name}\\E$"

        return true
    }

    override fun isConfigurationFromContext(
        configuration: AllureGoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val method = context.psiLocation as? GoMethodDeclaration ?: return false
        val suiteRunner = method.findAllureSuiteRunner() ?: return false

        return when {
            configuration.pattern != "^\\Q${suiteRunner.name}\\E$" -> false

            !configuration.params.contains("-allure-go.m ^\\Q${method.name}\\E$") -> false

            else -> true
        }
    }
}
