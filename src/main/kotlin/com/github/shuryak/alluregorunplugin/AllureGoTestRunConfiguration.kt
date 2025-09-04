package com.github.shuryak.alluregorunplugin

import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.collectGoFiles
import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.getGoPackage
import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.getReceiverGoType
import com.github.shuryak.alluregorunplugin.psi.GoPsiExtension.isAllureFrameworkProviderT
import com.goide.execution.GoBuildingRunConfiguration
import com.goide.execution.testing.GoTestFinder.isTestFunction
import com.goide.execution.testing.GoTestRunConfiguration
import com.goide.psi.GoCallExpr
import com.goide.psi.GoFile
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoType
import com.goide.psi.impl.GoBuiltinCallExprImpl
import com.goide.psi.impl.GoReferenceExpressionImpl
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

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
        val fn = method.name ?: return false
        if (!fn.startsWith("Test")) return false

        val params = method.signature?.parameters?.parameterDeclarationList ?: return false
        if (params.size != 1 || !params[0].isAllureFrameworkProviderT()) return false

        val suiteRunner = findSuiteRunner(method) ?: return false

        configuration.name = "allure-go ${method.name}"
        configuration.kind = GoBuildingRunConfiguration.Kind.PACKAGE
        configuration.`package` = suiteRunner.containingFile.getImportPath(true).toString()
        configuration.pattern = "^\\Q${suiteRunner.name}\\E$"
        configuration.workingDirectory = suiteRunner.containingFile?.virtualFile?.parent?.path.toString()
        configuration.params = "-test.count=1 -allure-go.m ^\\Q${method.name}\\E$"

        return true
    }

    override fun isConfigurationFromContext(
        configuration: AllureGoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val method = context.psiLocation as? GoMethodDeclaration ?: return false
        val suiteRunner = findSuiteRunner(method) ?: return false

        return when {
            configuration.pattern != "^\\Q${suiteRunner.name}\\E$" -> false

            !configuration.params.contains("-allure-go.m ^\\Q${method.name}\\E$") -> false

            else -> true
        }
    }

    fun findSuiteRunner(method: GoMethodDeclaration): GoFunctionOrMethodDeclaration? {
        val receiverType = method.getReceiverGoType() ?: return null
        val receiverContainingFile = receiverType.typeReferenceExpression?.resolve()?.containingFile

        findSuiteRunner(receiverContainingFile as GoFile, receiverType)?.let { return it }

        method.getGoPackage()?.collectGoFiles()?.forEach { file ->
            findSuiteRunner(file, receiverType)?.let { return it }
        }

        return null
    }

    fun findSuiteRunner(goFile: GoFile, suite: GoType): GoFunctionOrMethodDeclaration? {
        val calls = PsiTreeUtil.findChildrenOfType(goFile, GoCallExpr::class.java)

        calls.forEach { call ->
            val expr = call.expression
            if (expr is GoReferenceExpressionImpl && expr.text == "suite.RunNamedSuite") {
                call.argumentList.expressionList.forEach { arg ->
                    if (
                        arg is GoBuiltinCallExprImpl && arg.expression.text == "new" &&
                        arg.builtinArgumentList.type?.text == suite.text
                    ) {
                        val parent = PsiTreeUtil.getParentOfType(call, GoFunctionOrMethodDeclaration::class.java)

                        if (parent == null || !isTestFunction(parent)) {
                            return null
                        }

                        return parent
                    }
                }
            }
        }

        return null
    }
}

class AllureGoTestRunConfiguration(project: Project, name: String, configurationType: ConfigurationType) :
    GoTestRunConfiguration(project, name, configurationType)

class AllureGoTestRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String {
        return type.id
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        AllureGoTestRunConfiguration(project, "AllureGoTestRunConfiguration", type)
}

class AllureGoTestConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Allure Go Test"

    override fun getConfigurationTypeDescription(): String = "Allure Go test configuration"

    override fun getIcon(): Icon = AllIcons.Language.GO

    override fun getId(): String = "ALLURE_GO_TEST_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(AllureGoTestRunConfigurationFactory(this))

    companion object {
        val instance: AllureGoTestConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(AllureGoTestConfigurationType::class.java)
    }
}

class RunAllureGoTestAction : AnAction("Run Allure Go Test", "Description", AllIcons.RunConfigurations.TestState.Run) {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val runManager = RunManager.getInstance(project)

        val configurationSettings: RunnerAndConfigurationSettings =
            runManager.createConfiguration("Allure Go Test", AllureGoTestConfigurationType().configurationFactories[0])

        val executor = DefaultRunExecutor.getRunExecutorInstance()

        val environment = ExecutionEnvironmentBuilder.create(executor, configurationSettings).build()
        environment.runner.execute(environment)
    }
}
