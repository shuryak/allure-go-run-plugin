package com.github.shuryak.alluregorunplugin

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

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
